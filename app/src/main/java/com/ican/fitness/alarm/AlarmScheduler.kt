package com.ican.fitness.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ican.fitness.data.local.UserProfile
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId

object AlarmScheduler {

    private const val ALARM_REQUEST_CODE = 7001
    private const val SNOOZE_REQUEST_CODE = 7002

    fun scheduleNextWorkoutAlarm(context: Context, profile: UserProfile) {
        cancelAllWorkoutAlarms(context)
        if (!profile.alarmEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextTriggerMillis = computeNextAlarmTriggerMillis(
            hour = profile.alarmHour,
            minute = profile.alarmMinute,
            weeklyDaysCsv = profile.weeklyWorkoutDays
        ) ?: return

        val intent = Intent(context, WorkoutAlarmReceiver::class.java).apply {
            action = "com.ican.fitness.ACTION_WORKOUT_ALARM"
            putExtra("USER_NAME", profile.name.ifBlank { "Athlete" })
            putExtra("GOAL_TITLE", profile.primaryGoal)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactSafe(alarmManager, nextTriggerMillis, pendingIntent)
    }

    fun scheduleSnooze(context: Context, userName: String, goalTitle: String, minutes: Int = 10) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = System.currentTimeMillis() + minutes * 60_000L

        val intent = Intent(context, WorkoutAlarmReceiver::class.java).apply {
            action = "com.ican.fitness.ACTION_WORKOUT_ALARM"
            putExtra("USER_NAME", userName)
            putExtra("GOAL_TITLE", goalTitle)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SNOOZE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactSafe(alarmManager, triggerAt, pendingIntent)
    }

    fun cancelAllWorkoutAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        listOf(ALARM_REQUEST_CODE, SNOOZE_REQUEST_CODE).forEach { code ->
            val intent = Intent(context, WorkoutAlarmReceiver::class.java).apply {
                action = "com.ican.fitness.ACTION_WORKOUT_ALARM"
            }
            val pending = PendingIntent.getBroadcast(
                context,
                code,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pending != null) {
                alarmManager.cancel(pending)
                pending.cancel()
            }
        }
    }

    private fun scheduleExactSafe(alarmManager: AlarmManager, triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent),
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } else {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent),
                pendingIntent
            )
        }
    }

    fun computeNextAlarmTriggerMillis(hour: Int, minute: Int, weeklyDaysCsv: String): Long? {
        val now = LocalDateTime.now()
        val allowedDays = weeklyDaysCsv.split(",")
            .map { it.trim().uppercase() }
            .filter { it.isNotEmpty() }
            .toSet()

        for (offsetDays in 0..7) {
            val candidateDate = now.toLocalDate().plusDays(offsetDays.toLong())
            val candidateDateTime = candidateDate.atTime(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
            if (candidateDateTime.isAfter(now.plusSeconds(5))) {
                val dayCode = when (candidateDate.dayOfWeek) {
                    DayOfWeek.MONDAY -> "MON"
                    DayOfWeek.TUESDAY -> "TUE"
                    DayOfWeek.WEDNESDAY -> "WED"
                    DayOfWeek.THURSDAY -> "THU"
                    DayOfWeek.FRIDAY -> "FRI"
                    DayOfWeek.SATURDAY -> "SAT"
                    DayOfWeek.SUNDAY -> "SUN"
                }
                if (allowedDays.isEmpty() || allowedDays.contains(dayCode)) {
                    return candidateDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }
            }
        }
        return null
    }
}
