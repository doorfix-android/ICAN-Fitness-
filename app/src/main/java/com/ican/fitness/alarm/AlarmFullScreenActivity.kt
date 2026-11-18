package com.ican.fitness.alarm

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.ican.fitness.MainActivity
import com.ican.fitness.data.local.UserPreferencesRepository
import com.ican.fitness.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AlarmFullScreenActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    private val funnyQuotes = listOf(
        "Your muscles won't build themselves while you argue with the snooze button!",
        "5 more minutes of sleep won't give you 5 more reps. Let's move!",
        "Gravity is waiting to test you. Show it who's boss!",
        "Future you is already thanking present you for getting out of bed.",
        "Train Smart. Eat Right. Become Stronger. ICAN starts right now!"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            km.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        startLoopingAlarmSound()

        val userName = intent.getStringExtra("USER_NAME") ?: "Athlete"
        val goalTitle = intent.getStringExtra("GOAL_TITLE") ?: "Daily Workout"
        val quote = funnyQuotes.random()
        val timeText = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))

        setContent {
            IcanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CharcoalBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.height(28.dp))
                            Text(
                                text = "ICAN WORKOUT ALARM",
                                color = OliveAccent,
                                style = MaterialTheme.typography.labelLarge,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = timeText,
                                color = WarmCream,
                                fontSize = 52.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Rise & Train, $userName",
                                color = WarmCreamMuted,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "FOCUS: $goalTitle",
                                    color = OliveAccent,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "\"$quote\"",
                                    color = WarmCream,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    stopSoundAndDismissNotification()
                                    rescheduleNextDayAlarm()
                                    val launchApp = Intent(this@AlarmFullScreenActivity, MainActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    }
                                    startActivity(launchApp)
                                    finish()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OliveAccent,
                                    contentColor = CharcoalBg
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Text("STOP & START TODAY'S PLAN", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    stopSoundAndDismissNotification()
                                    AlarmScheduler.scheduleSnooze(this@AlarmFullScreenActivity, userName, goalTitle, 10)
                                    finish()
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Text("SNOOZE 10 MINUTES", color = WarmCream)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startLoopingAlarmSound() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmFullScreenActivity, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (_: Exception) {
        }
    }

    private fun stopSoundAndDismissNotification() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(9001)
    }

    private fun rescheduleNextDayAlarm() {
        lifecycleScope.launch {
            val prefs = UserPreferencesRepository(this@AlarmFullScreenActivity)
            val profile = prefs.userProfileFlow.first()
            if (profile.alarmEnabled) {
                AlarmScheduler.scheduleNextWorkoutAlarm(this@AlarmFullScreenActivity, profile)
            }
        }
    }

    override fun onDestroy() {
        stopSoundAndDismissNotification()
        super.onDestroy()
    }
}
