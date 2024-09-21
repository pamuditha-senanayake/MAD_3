package com.example.task

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var containerLayout: LinearLayout  // Use LinearLayout instead of ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the LinearLayout where items will be added
        containerLayout = findViewById(R.id.linearLayout)

        // Initialize the FloatingActionButton
        val fab: FloatingActionButton = findViewById(R.id.fab)

        // Set the click listener for the FAB
        fab.setOnClickListener {
            showInputDialog()
        }
    }

    // Function to display the dialog input
    private fun showInputDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Time")

        // Inflate the custom input dialog
        val inflater: LayoutInflater = layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_input, null)
        builder.setView(dialogView)

        val inputField: EditText = dialogView.findViewById(R.id.editTextTime)

        // Set up the "Add" button in the dialog
        builder.setPositiveButton("Add") { _, _ ->
            val timeInput = inputField.text.toString()
            if (timeInput.isNotEmpty()) {
                addTimeToLayout(timeInput)
            }
        }

        // Set up the "Cancel" button in the dialog
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    // Function to dynamically add a new item to the layout
    private fun addTimeToLayout(time: String) {
        // Inflate the new time view (from time_item.xml layout)
        val newTimeView = LayoutInflater.from(this).inflate(R.layout.time_item, null) as ConstraintLayout

        // Set the time in the TextView inside the new view
        val timeTextView = newTimeView.findViewById<TextView>(R.id.textViewTime)
        timeTextView.text = time

        // Define layout parameters for the new view
        val layoutParams = LinearLayout.LayoutParams(250, 100) // Set width to 250dp, height to 100dp
        layoutParams.setMargins(16, 0, 0, 0)  // Add left margin for spacing
        layoutParams.gravity = android.view.Gravity.CENTER_VERTICAL  // Center vertically

        // Add the new time view to the container layout (LinearLayout)
        containerLayout.addView(newTimeView, layoutParams)
    }
}
