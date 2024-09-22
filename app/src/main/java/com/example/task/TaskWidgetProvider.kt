package com.example.task

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import java.util.concurrent.TimeUnit

class TaskWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // This method is called only once, when the first widget is created
        // You can use it for initialization purposes
    }

    override fun onDisabled(context: Context) {
        // This method is called only once, when the last widget is disabled
        // You can use it to clean up resources
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Check if the intent is for this widget
        if (intent.action == ACTION_TASK_COMPLETE) {
            val appWidgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                updateAppWidget(context, appWidgetManager, appWidgetId)
                Toast.makeText(context, "Task completed in widget!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        // Action and extra for updating the widget
        const val ACTION_TASK_COMPLETE = "com.example.task.ACTION_TASK_COMPLETE"
        const val EXTRA_APPWIDGET_ID = "appWidgetId"
        const val EXTRA_TASK_NAME = "taskName"
        const val EXTRA_TIME_REMAINING = "timeRemaining"
    }
}

// Helper function to update the widget
fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val views = RemoteViews(context.packageName, R.layout.task_widget)

    // Get the last completed task from SharedPreferences
    val lastCompletedTask = context.getSharedPreferences("TimePrefs", Context.MODE_PRIVATE)
        .getString("lastCompletedTask", null)

    // If a task is running, display the time remaining
    if (lastCompletedTask != null) {
        views.setTextViewText(R.id.appwidget_text, "Task $lastCompletedTask completed!")
    } else {
        // If no task is running, display a default message
        views.setTextViewText(R.id.appwidget_text, context.getString(R.string.appwidget_text))
    }

    // Create an Intent to launch the main activity when the widget is clicked
    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

    views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}