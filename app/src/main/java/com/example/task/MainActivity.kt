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
    private val taskList = mutableMapOf<String, MutableList<Triple<String, String, String>>>() // Tasks organized by time frame (name, duration, description)
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
                val description = popupView.findViewById<EditText>(R.id.editTextRoutine).text.toString()

                if (taskName.isNotEmpty() && duration.isNotEmpty() && selectedTimeFrame != null) {
                    addTaskToLayout(taskName, duration, description, selectedTimeFrame!!)
                    saveTaskToSharedPreferences(taskName, duration, description, selectedTimeFrame!!)
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

    private fun addTaskToLayout(taskName: String, taskDuration: String, description: String, timeFrame: String) {
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
        taskLayout.findViewById<TextView>(R.id.textViewDescription).text = description

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

        // Add a click listener to the task layout to allow for updating tasks
        taskLayout.setOnClickListener {
            showUpdateTaskDialog(taskName, taskDuration, description, timeFrame)
        }

        container.addView(taskLayout)

        // Add the task to the taskList map
        taskList.getOrPut(timeFrame) { mutableListOf() }
            .add(Triple(taskName, taskDuration, description))
    }

    // Update existing task
    fun updateTask(taskName: String, taskDuration: String, taskDescription: String, timeFrame: String) {
        // Update the task in the taskList
        taskList[timeFrame]?.forEachIndexed { index, task ->
            if (task.first == taskName) {
                taskList[timeFrame]?.set(index, Triple(taskName, taskDuration, taskDescription))
            }
        }
        saveTaskToSharedPreferences(taskName, taskDuration, taskDescription, timeFrame)
        updateTaskListDisplay()
    }

    private fun saveTaskToSharedPreferences(taskName: String, duration: String, description: String, timeFrame: String) {
        with(sharedPreferences.edit()) {
            val existingTasks = taskList[timeFrame]?.map { it.first } ?: emptyList()
            existingTasks + taskName // Adding the new task to the existing tasks
            putStringSet("${timeFrame}_tasks", existingTasks.toSet())
            putString("${timeFrame}_${taskName}_description", description) // Save description
            apply()
        }
    }

    private fun loadSavedTasks() {
        for (time in timeList) {
            val tasks = sharedPreferences.getStringSet("${time}_tasks", emptySet()) ?: emptySet()

            // Initialize taskList[time] if it doesn't exist
            taskList.getOrPut(time) { mutableListOf() }

            // Load tasks with description
            tasks.forEach { taskName ->
                val description = sharedPreferences.getString("${time}_${taskName}_description", "")
                taskList[time]?.add(Triple(taskName, "Task Duration", description.orEmpty())) // Add task with dummy duration
            }
        }
    }

    private fun removeTaskFromSharedPreferences(taskName: String, timeFrame: String) {
        taskList[timeFrame]?.removeIf { it.first == taskName }
        with(sharedPreferences.edit()) {
            val existingTasks = taskList[timeFrame]?.map { it.first } ?: emptyList()
            putStringSet("${timeFrame}_tasks", existingTasks.toSet())
            remove("${timeFrame}_${taskName}_description") // Remove description
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
            taskList[timeFrame]?.forEach { (taskName, duration, description) ->
                val container = findViewById<LinearLayout>(R.id.linearLayout4)
                val taskLayout = LayoutInflater.from(this).inflate(R.layout.task_item, container, false) as ConstraintLayout

                taskLayout.findViewById<TextView>(R.id.textViewTask).text = taskName
                taskLayout.findViewById<TextView>(R.id.textViewDuration).text = duration
                // Update description text view in your task_item.xml
                taskLayout.findViewById<TextView>(R.id.textViewDescription).text = description

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

                // Add a click listener to the task layout to allow for updating tasks
                taskLayout.setOnClickListener {
                    showUpdateTaskDialog(taskName, duration, description, timeFrame)
                }

                container.addView(taskLayout)
            }
        }
    }

    private fun showUpdateTaskDialog(taskName: String, duration: String, description: String, timeFrame: String) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.dialog_task_input, null)
        popupView.findViewById<EditText>(R.id.editTextTaskName).setText(taskName)
        popupView.findViewById<EditText>(R.id.editTextDuration).setText(duration)
        popupView.findViewById<EditText>(R.id.editTextRoutine).setText(description) // Set the description

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(popupView)
            .setTitle("Update Task")
            .setPositiveButton("Update Task") { dialog, _ ->
                val updatedTaskName = popupView.findViewById<EditText>(R.id.editTextTaskName).text.toString()
                val updatedDuration = popupView.findViewById<EditText>(R.id.editTextDuration).text.toString()
                val updatedDescription = popupView.findViewById<EditText>(R.id.editTextRoutine).text.toString()

                updateTask(updatedTaskName, updatedDuration, updatedDescription, timeFrame)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        dialogBuilder.create().show()
    }
}