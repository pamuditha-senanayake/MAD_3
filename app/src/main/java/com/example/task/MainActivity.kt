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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        containerLayout = findViewById(R.id.linearLayout)
        sharedPreferences = getSharedPreferences("TimePrefs", Context.MODE_PRIVATE)

        loadSavedTimes()

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            showInputDialog()
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
}
