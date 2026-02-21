package com.example.quanlylichhoc.utils

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.quanlylichhoc.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", 0)
        val title = intent.getStringExtra("title") ?: "Nhắc nhở"
        val message = intent.getStringExtra("message") ?: ""
        val type = intent.getStringExtra("type") ?: ""

        if (type == "daily_summary") {
            handleDailySummary(context)
        } else {
            showNotification(context, id, title, message)
        }

        // Reschedule weekly for individual classes
        if (type == "class") {
            val nextWeek = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000
            NotificationHelper.scheduleNotification(context, id, title, message, nextWeek, type)
        }
    }

    private fun handleDailySummary(context: Context) {
        val dbHelper = com.example.quanlylichhoc.database.DatabaseHelper(context)
        val calendar = java.util.Calendar.getInstance()
        val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        
        val classes = dbHelper.getClassesByDay(dayOfWeek)
        
        if (classes.isNotEmpty()) {
            val summary = StringBuilder()
            classes.forEach { 
                summary.append("• ${it.subjectName}: ${it.startTime} - ${it.room}\n")
            }
            showNotification(context, 9999, "Lịch học hôm nay", summary.toString().trim())
        }
        
        // Reschedule for tomorrow 5 AM
        NotificationHelper.scheduleDailySummary(context)
    }

    private fun showNotification(context: Context, id: Int, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, "quanlylichhoc_notifications")
            .setSmallIcon(R.drawable.ic_notifications) // Ensure this exists or use a fallback
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        notificationManager.notify(id, builder.build())
    }
}
