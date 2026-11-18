package com.ican.fitness.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class WorkoutAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val userName = intent.getStringExtra("USER_NAME") ?: "Athlete"
        val goalTitle = intent.getStringExtra("GOAL_TITLE") ?: "Daily Training"

        val fullScreenIntent = Intent(context, AlarmFullScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("USER_NAME", userName)
            putExtra("GOAL_TITLE", goalTitle)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            8001,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "ican_gym_alarm_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "ICAN Workout Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Full-screen gym wake-up alarm"
                setBypassDnd(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("ICAN Gym Time, $userName!")
            .setContentText("Goal: $goalTitle — Tap to open workout coach.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .build()

        notificationManager.notify(9001, notification)

        try {
            context.startActivity(fullScreenIntent)
        } catch (_: Exception) {
            // Android 14+ background launch restrictions will rely on fullScreenIntent notification
        }
    }
}
