package com.example.task

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var containerLayout: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private val timeList = mutableListOf<String>() // Time frames (hours)
    private var taskCounter = 0 // For unique task ID
    private val taskList = mutableMapOf<String, MutableList<Pair<String, String>>>() // Tasks organized by time frame
    private var selectedTimeFrame: String? = null // Store the selected time frame

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        containerLayout = findViewById(R.id.linearLayout)
        sharedPreferences = getSharedPreferences("TimePrefs", Context.MODE_PRIVATE)

        loadSavedTimes()
        loadSavedTasks() // Load tasks from SharedPreferences

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            showInputDialog()
        }

        val fab2: FloatingActionButton = findViewById(R.id.fab2)
        fab2.setOnClickListener {
            showPopupDialog()
        }
    }

    private fun showInputDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Time")

        val inflater: LayoutInflater = layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_input, null)
        builder.setView(dialogView)

        val inputField: EditText = dialogView.findViewById(R.id.editTextTime)

        builder.setPositiveButton("Add") { _, _ ->
            val timeInput = inputField.text.toString()
            if (timeInput.isNotEmpty()) {
                addTimeToLayout(timeInput)
                saveTimeToSharedPreferences(timeInput)
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun addTimeToLayout(time: String) {
        // Check if the time frame already exists
        if (timeList.contains(time)) {
            AlertDialog.Builder(this)
                .setTitle("Time Frame Exists")
                .setMessage("A time frame with this time already exists.")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
            return
        }

        val newTimeView = LayoutInflater.from(this).inflate(R.layout.time_item, null) as ConstraintLayout
        val timeTextView = newTimeView.findViewById<TextView>(R.id.textViewTime)
        timeTextView.text = time

        // Set up the listener for the TextViewTime (for selection)
        timeTextView.setOnClickListener {
            // Deselect all other time frame TextViews
            for (i in 0 until containerLayout.childCount) {
                val child = containerLayout.getChildAt(i) as ConstraintLayout
                val childTextView = child.findViewById<TextView>(R.id.textViewTime)
                childTextView.isSelected = false
            }

            // Select this time frame TextView
            timeTextView.isSelected = true
            selectedTimeFrame = time // Update selectedTimeFrame
            updateTaskListDisplay() // Update the task list display
        }

        val deleteButton = newTimeView.findViewById<Button>(R.id.button)

        // Set up the listener for deleting the time frame and tasks
        deleteButton.setOnClickListener {
            containerLayout.removeView(newTimeView)
            removeTimeFromSharedPreferences(time)
            // Remove tasks associated with the deleted time frame
            taskList.remove(time)
            updateSelectedTimeFrame() // Call to update the selected time frame
            updateTaskListDisplay() // Update the task list display
            selectedTimeFrame = null // Reset selectedTimeFrame
        }

        val layoutParams = LinearLayout.LayoutParams(300, 120)
        layoutParams.setMargins(16, 0, 0, 0)
        layoutParams.gravity = android.view.Gravity.CENTER_VERTICAL

        // Add the new time frame view to the containerLayout
        containerLayout.addView(newTimeView, layoutParams)
    }

    // Helper function to update selectedTimeFrame and update the task list display
    private fun updateSelectedTimeFrame() {
        for (i in 0 until containerLayout.childCount) {
            val child = containerLayout.getChildAt(i) as ConstraintLayout
            val selectedTextView = child.findViewById<TextView>(R.id.textViewTime)
            if (selectedTextView.isSelected) {
                selectedTimeFrame = selectedTextView.text.toString()
                updateTaskListDisplay() // Update the task list display
                return // Exit the loop after finding the selected time frame
            }
        }
        selectedTimeFrame = null // Reset selectedTimeFrame if no time frame is selected
        updateTaskListDisplay() // Update the task list display
    }

    private fun saveTimeToSharedPreferences(time: String) {
        timeList.add(time)
        with(sharedPreferences.edit()) {
            putStringSet("saved_times", timeList.toSet())
            apply()
        }
    }

    private fun loadSavedTimes() {
        val savedTimes = sharedPreferences.getStringSet("saved_times", emptySet())
        savedTimes?.forEach { time ->
            addTimeToLayout(time)
        }
    }

    private fun removeTimeFromSharedPreferences(time: String) {
        timeList.remove(time)
        with(sharedPreferences.edit()) {
            putStringSet("saved_times", timeList.toSet())
            apply()
        }
    }

    private fun showPopupDialog() {
        // Check if a time frame is selected
        if (selectedTimeFrame == null) {
            AlertDialog.Builder(this)
                .setTitle("Select a Time Frame")
                .setMessage("Please select a time frame from Card 1 before adding a task.")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
            return
        }

        val popupView = LayoutInflater.from(this).inflate(R.layout.dialog_task_input, null)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(popupView)
            .setTitle("Add Task")
            .setPositiveButton("Add Task") { dialog, _ ->
                val taskName = popupView.findViewById<EditText>(R.id.editTextTaskName).text.toString()
                val duration = popupView.findViewById<EditText>(R.id.editTextDuration).text.toString()

                if (taskName.isNotEmpty() && duration.isNotEmpty() && selectedTimeFrame != null) {
                    addTaskToLayout(taskName, duration, selectedTimeFrame!!)
                    saveTaskToSharedPreferences(taskName, duration, selectedTimeFrame!!)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        dialogBuilder.create().show()
    }

    // Helper function to get the currently selected time frame
    private fun getSelectedTime(): String? {
        return containerLayout.findViewWithTag<TextView>(selectedTimeFrame)?.text?.toString()
    }

    private fun addTaskToLayout(taskName: String, taskDuration: String, timeFrame: String) {
        // Check if the task already exists
        if (taskList[timeFrame]?.any { it.first == taskName } == true) {
            AlertDialog.Builder(this)
                .setTitle("Task Exists")
                .setMessage("A task with this name already exists.")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
            return
        }

        val container = findViewById<LinearLayout>(R.id.linearLayout4)
        val taskLayout = LayoutInflater.from(this).inflate(R.layout.task_item, container, false) as ConstraintLayout

        taskLayout.findViewById<TextView>(R.id.textViewTask).text = taskName
        taskLayout.findViewById<TextView>(R.id.textViewDuration).text = taskDuration

        // Add a delete button to the task layout
        val deleteButton = taskLayout.findViewById<Button>(R.id.button)
        deleteButton.setOnClickListener {
            container.removeView(taskLayout)
            removeTaskFromSharedPreferences(taskName, timeFrame)
            // Remove the task from the taskList as well
            taskList[timeFrame]?.removeIf { it.first == taskName }
            updateTaskListDisplay() // Update the task list display
        }

        // Add a play button (for timer)
        val playButton = taskLayout.findViewById<Button>(R.id.buttonPlay)
        playButton.setOnClickListener {
            val durationText = taskLayout.findViewById<TextView>(R.id.textViewDuration).text.toString()
            val durationInMillis = convertDurationToMillis(durationText)
            startCountDownTimer(durationInMillis, taskLayout)
        }

        container.addView(taskLayout)

        // Add the task to the taskList map
        taskList.getOrPut(timeFrame) { mutableListOf() }
            .add(Pair(taskName, taskDuration))
    }


    private fun saveTaskToSharedPreferences(taskName: String, duration: String, timeFrame: String) {
        with(sharedPreferences.edit()) {
            val existingTasks = taskList[timeFrame]?.map { it.first } ?: emptyList()
            existingTasks + taskName // Adding the new task to the existing tasks
            putStringSet("${timeFrame}_tasks", existingTasks.toSet())
            apply()
        }
    }

    private fun loadSavedTasks() {
        for (time in timeList) {
            val tasks = sharedPreferences.getStringSet("${time}_tasks", emptySet()) ?: emptySet()
            taskList[time] = tasks.map { Pair(it, "Task Duration") }.toMutableList() // Load tasks with dummy duration
        }
    }

    private fun removeTaskFromSharedPreferences(taskName: String, timeFrame: String) {
        taskList[timeFrame]?.removeIf { it.first == taskName }
        with(sharedPreferences.edit()) {
            val existingTasks = taskList[timeFrame]?.map { it.first } ?: emptyList()
            putStringSet("${timeFrame}_tasks", existingTasks.toSet())
            apply()
        }
    }


    private fun convertDurationToMillis(duration: String): Long {
        val parts = duration.split(":").map { it.toIntOrNull() ?: 0 }
        return (parts[0] * 60 * 1000 + parts[1] * 1000).toLong() // Convert to milliseconds
    }

    private fun startCountDownTimer(durationInMillis: Long, taskLayout: ConstraintLayout) {
        val durationTextView = taskLayout.findViewById<TextView>(R.id.textViewDuration) // Use textViewDuration here
        object : CountDownTimer(durationInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                durationTextView.text = String.format("%02d:%02d", minutes, seconds) // Update textViewDuration
            }

            override fun onFinish() {
                durationTextView.text = "Finished!"
            }
        }.start()
    }



    private fun updateTaskListDisplay() {
        // Clear the existing task list display
        findViewById<LinearLayout>(R.id.linearLayout4).removeAllViews()

        // Display tasks for the selected time frame
        selectedTimeFrame?.let { timeFrame ->
            taskList[timeFrame]?.forEach { (taskName, duration) ->
                val container = findViewById<LinearLayout>(R.id.linearLayout4)
                val taskLayout = LayoutInflater.from(this).inflate(R.layout.task_item, container, false) as ConstraintLayout

                taskLayout.findViewById<TextView>(R.id.textViewTask).text = taskName
                taskLayout.findViewById<TextView>(R.id.textViewDuration).text = duration

                // Add a delete button to the task layout
                val deleteButton = taskLayout.findViewById<Button>(R.id.button)
                deleteButton.setOnClickListener {
                    container.removeView(taskLayout)
                    removeTaskFromSharedPreferences(taskName, timeFrame)
                    // Remove the task from the taskList as well
                    taskList[timeFrame]?.removeIf { it.first == taskName }
                    updateTaskListDisplay() // Update the task list display
                }

                // Add a play button (for timer)
                val playButton = taskLayout.findViewById<Button>(R.id.buttonPlay)
                playButton.setOnClickListener {
                    val durationText = taskLayout.findViewById<TextView>(R.id.textViewDuration).text.toString()
                    val durationInMillis = convertDurationToMillis(durationText)
                    startCountDownTimer(durationInMillis, taskLayout)
                }

                container.addView(taskLayout)
            }
        }
    }
}
