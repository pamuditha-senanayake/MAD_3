package com.example.task

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class MainActivity2 : AppCompatActivity() {
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Find the BottomNavigationView *after* the layout is inflated
        findViewById<BottomNavigationView>(R.id.bottomNavigationView)?.let { bottomNavigationView ->
            bottomNavigationView.setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.home -> {
                        // Already in ms1 (Home), so do nothing or reload this activity

                        val intent = Intent(this, MainActivity::class.java) // Navigate to ms2 (Notes)
                        startActivity(intent)
                        true
                    }
                    R.id.alarm -> {
                        true
                    }
                    else -> false
                }
            }
        }

        // Set up the button to set the alarm
        val setAlarmButton: Button = findViewById(R.id.setAlarmButton)
        setAlarmButton.setOnClickListener {
            val alarmTimeInput: EditText = findViewById(R.id.alarmTimeInput)
            val alarmTimeString = alarmTimeInput.text.toString()

            // You'll need to parse the alarmTimeString into hours, minutes, and seconds
            // For simplicity, let's assume the format is "HH:mm:ss"
            val timeParts = alarmTimeString.split(":")
            if (timeParts.size == 3) {
                val hours = timeParts[0].toIntOrNull()
                val minutes = timeParts[1].toIntOrNull()
                val seconds = timeParts[2].toIntOrNull()

                if (hours != null && minutes != null && seconds != null) {
                    setAlarm(hours, minutes, seconds)
                    Toast.makeText(
                        this,
                        "Alarm set for $alarmTimeString",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Invalid time format. Use HH:mm:ss",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Invalid time format. Use HH:mm:ss",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setAlarm(hour: Int, minute: Int, second: Int) {
        // 1. Create an Intent to trigger the AlarmReceiver when the alarm goes off
        val intent = Intent(this, AlarmReceiver::class.java)

        // 2. Create a PendingIntent that wraps the Intent and allows you to send it later
        pendingIntent = PendingIntent.getBroadcast(
            this,
            0, // Request code (can be any unique integer)
            intent,
            PendingIntent.FLAG_IMMUTABLE // Use FLAG_IMMUTABLE for PendingIntents that don't need to be updated
        )

        // 3. Get a Calendar instance to set the alarm time
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, second)
        calendar.set(Calendar.MILLISECOND, 0)

        // 4. Set the alarm using AlarmManager
        alarmManager.set(
            AlarmManager.RTC_WAKEUP, // Use RTC_WAKEUP to wake the device up
            calendar.timeInMillis,
            pendingIntent
        )
    }

    // You can cancel the alarm if needed:
    fun cancelAlarm() {
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}