package com.example.task

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var containerLayout: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private val timeList = mutableListOf<String>()
    private var taskCounter = 0 // For unique task ID
    private val taskList = mutableListOf<Pair<String, String>>() // To store task name and duration

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
        val newTimeView = LayoutInflater.from(this).inflate(R.layout.time_item, null) as ConstraintLayout
        val timeTextView = newTimeView.findViewById<TextView>(R.id.textViewTime)
        timeTextView.text = time

        val deleteButton = newTimeView.findViewById<Button>(R.id.button)
        deleteButton.setOnClickListener {
            containerLayout.removeView(newTimeView)
            removeTimeFromSharedPreferences(time)
        }

        val layoutParams = LinearLayout.LayoutParams(300, 120)
        layoutParams.setMargins(16, 0, 0, 0)
        layoutParams.gravity = android.view.Gravity.CENTER_VERTICAL

        containerLayout.addView(newTimeView, layoutParams)
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
        val popupView = LayoutInflater.from(this).inflate(R.layout.dialog_task_input, null)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(popupView)
            .setTitle("Add Task")
            .setPositiveButton("Add Task") { dialog, _ ->
                val taskName = popupView.findViewById<EditText>(R.id.editTextTaskName).text.toString()
                val duration = popupView.findViewById<EditText>(R.id.editTextDuration).text.toString()

                if (taskName.isNotEmpty() && duration.isNotEmpty()) {
                    addTaskToLayout(taskName, duration)
                    saveTaskToSharedPreferences(taskName, duration)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        dialogBuilder.create().show()
    }

    private fun addTaskToLayout(taskName: String, taskDuration: String) {
        val container = findViewById<LinearLayout>(R.id.linearLayout4)
        val taskLayout = LayoutInflater.from(this).inflate(R.layout.task_item, container, false)

        taskLayout.findViewById<TextView>(R.id.textViewTask).text = taskName
        taskLayout.findViewById<TextView>(R.id.textViewDuration).text = taskDuration

        // Add a delete button to the task layout
        val deleteButton = taskLayout.findViewById<Button>(R.id.button)
        deleteButton.setOnClickListener {
            container.removeView(taskLayout)
            removeTaskFromSharedPreferences(taskCounter) // Remove task from SharedPreferences
            taskCounter-- // Decrement the counter
        }

        container.addView(taskLayout)
        taskList.add(Pair(taskName, taskDuration)) // Update the taskList for deletion later
    }

    private fun saveTaskToSharedPreferences(taskName: String, duration: String) {
        val taskKey = "task_$taskCounter" // Unique key for each task
        with(sharedPreferences.edit()) {
            putString(taskKey, "$taskName::$duration") // Store task name and duration
            apply()
        }
        taskCounter++
    }

    private fun loadSavedTasks() {
        val container = findViewById<LinearLayout>(R.id.linearLayout4)

        // Get the actual task counter from SharedPreferences
        taskCounter = 0 // Reset taskCounter first
        var key = "task_0"
        while (sharedPreferences.contains(key)) {
            taskCounter++
            key = "task_$taskCounter"
        }

        // Load tasks based on the actual taskCounter
        for (i in 0 until taskCounter) {
            val taskKey = "task_$i"
            val taskString = sharedPreferences.getString(taskKey, null)
            if (taskString != null) {
                val parts = taskString.split("::")
                if (parts.size == 2) {
                    addTaskToLayout(parts[0], parts[1]) // Add the task to the layout
                }
            }
        }
    }

    private fun removeTaskFromSharedPreferences(taskIndex: Int) {
        val taskKey = "task_$taskIndex"
        with(sharedPreferences.edit()) {
            remove(taskKey)
            apply()
        }
        if (taskIndex < taskList.size) {
            taskList.removeAt(taskIndex) // Remove task from the taskList
        }
    }
}