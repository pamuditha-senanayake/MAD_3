package com.example.task

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Play alarm sound
        val mediaPlayer = MediaPlayer.create(context, R.raw.notification_sound)
        mediaPlayer.start() // Replace R.raw.alarm_sound with your actual alarm sound resource

        // Display a notification (optional)
        Toast.makeText(context, "Alarm ringing!", Toast.LENGTH_LONG).show()
    }
}