package com.ican.fitness.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ican.fitness.data.local.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefs = UserPreferencesRepository(context)
                    val profile = prefs.userProfileFlow.first()
                    if (profile.alarmEnabled) {
                        AlarmScheduler.scheduleNextWorkoutAlarm(context, profile)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

