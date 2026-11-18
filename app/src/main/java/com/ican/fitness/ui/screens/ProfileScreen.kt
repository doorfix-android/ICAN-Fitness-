package com.ican.fitness.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ican.fitness.alarm.AlarmScheduler
import com.ican.fitness.data.local.UserProfile
import com.ican.fitness.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    profile: UserProfile,
    onSaveProfile: (UserProfile) -> Unit,
    onExportDataJson: suspend () -> String,
    onWipeAllData: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember(profile) { mutableStateOf(profile.name) }
    var weightText by remember(profile) { mutableStateOf(profile.weightKg.toString()) }
    var heightText by remember(profile) { mutableStateOf(profile.heightCm.toString()) }
    var dietPref by remember(profile) { mutableStateOf(profile.dietaryPreference) }
    var allergies by remember(profile) { mutableStateOf(profile.foodAllergies) }
    var equipment by remember(profile) { mutableStateOf(profile.availableEquipment) }
    var durationMin by remember(profile) { mutableIntStateOf(profile.workoutDurationMinutes) }
    var beginnerMode by remember(profile) { mutableStateOf(profile.beginnerMode) }
    var hydrationReminders by remember(profile) { mutableStateOf(profile.hydrationReminders) }
    var mealReminders by remember(profile) { mutableStateOf(profile.mealReminders) }

    var alarmEnabled by remember(profile) { mutableStateOf(profile.alarmEnabled) }
    var alarmHourText by remember(profile) { mutableStateOf(profile.alarmHour.toString().padStart(2, '0')) }
    var alarmMinuteText by remember(profile) { mutableStateOf(profile.alarmMinute.toString().padStart(2, '0')) }

    var savedBanner by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            containerColor = GraphiteSurface,
            title = { Text("Reset All Data?", color = WarmCream) },
            text = { Text("This deletes all workouts, goals, meal plans, and resets onboarding.", color = WarmCreamMuted) },
            confirmButton = {
                TextButton(onClick = {
                    AlarmScheduler.cancelAllWorkoutAlarms(context)
                    showResetConfirm = false
                    onWipeAllData()
                }) {
                    Text("Erase Everything", color = TerracottaAlert, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel", color = WarmCreamMuted)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text("SETTINGS & PROFILE", color = OliveAccent, style = MaterialTheme.typography.labelLarge)
        Text("Personalize ICAN", color = WarmCream, style = MaterialTheme.typography.headlineLarge)

        if (savedBanner) {
            Card(colors = CardDefaults.cardColors(containerColor = OliveContainer), shape = RoundedCornerShape(12.dp)) {
                Text(
                    text = "Settings & alarm schedule saved!",
                    color = WarmCream,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // BODY & NUTRITION PREFS
        Card(colors = CardDefaults.cardColors(containerColor = GraphiteSurface), shape = RoundedCornerShape(18.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Athlete Details", color = OliveAccent, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Text("Dietary Preference", color = WarmCream, style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Vegetarian", "Eggetarian", "Non-Vegetarian", "Vegan").forEach { d ->
                        FilterChip(selected = dietPref == d, onClick = { dietPref = d }, label = { Text(d) })
                    }
                }

                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Food Allergies") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = equipment,
                    onValueChange = { equipment = it },
                    label = { Text("Available Equipment") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Workout Duration (minutes)", color = WarmCream, style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(25, 35, 45, 60).forEach { m ->
                        FilterChip(selected = durationMin == m, onClick = { durationMin = m }, label = { Text("$m min") })
                    }
                }
            }
        }

        // GYM ALARM & COACHING MODES
        Card(colors = CardDefaults.cardColors(containerColor = GraphiteSurface), shape = RoundedCornerShape(18.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Alarm & Coaching Preferences", color = OliveAccent, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Full-Screen Gym Alarm", color = WarmCream)
                    Switch(checked = alarmEnabled, onCheckedChange = { alarmEnabled = it })
                }

                if (alarmEnabled) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = alarmHourText,
                            onValueChange = { alarmHourText = it },
                            label = { Text("Hour (00-23)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = alarmMinuteText,
                            onValueChange = { alarmMinuteText = it },
                            label = { Text("Minute (00-59)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Beginner Guidance Mode", color = WarmCream)
                    Switch(checked = beginnerMode, onCheckedChange = { beginnerMode = it })
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Hydration Reminders", color = WarmCream)
                    Switch(checked = hydrationReminders, onCheckedChange = { hydrationReminders = it })
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Meal Reminders", color = WarmCream)
                    Switch(checked = mealReminders, onCheckedChange = { mealReminders = it })
                }
            }
        }

        Button(
            onClick = {
                val h = alarmHourText.toIntOrNull()?.coerceIn(0, 23) ?: profile.alarmHour
                val m = alarmMinuteText.toIntOrNull()?.coerceIn(0, 59) ?: profile.alarmMinute
                val updated = profile.copy(
                    name = name.trim().ifBlank { "Athlete" },
                    heightCm = heightText.toFloatOrNull()?.coerceIn(100f, 240f) ?: profile.heightCm,
                    weightKg = weightText.toFloatOrNull()?.coerceIn(30f, 250f) ?: profile.weightKg,
                    dietaryPreference = dietPref,
                    foodAllergies = allergies.trim().ifBlank { "None" },
                    availableEquipment = equipment.trim().ifBlank { "Bodyweight" },
                    workoutDurationMinutes = durationMin,
                    beginnerMode = beginnerMode,
                    hydrationReminders = hydrationReminders,
                    mealReminders = mealReminders,
                    alarmEnabled = alarmEnabled,
                    alarmHour = h,
                    alarmMinute = m
                )
                if (alarmEnabled) {
                    AlarmScheduler.scheduleNextWorkoutAlarm(context, updated)
                } else {
                    AlarmScheduler.cancelAllWorkoutAlarms(context)
                }
                onSaveProfile(updated)
                savedBanner = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = OliveAccent, contentColor = CharcoalBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("SAVE ALL CHANGES", fontWeight = FontWeight.Bold)
        }

        // BACKUP & RESET
        OutlinedButton(
            onClick = {
                scope.launch {
                    val json = onExportDataJson()
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "ICAN Fitness Backup")
                        putExtra(Intent.EXTRA_TEXT, json)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Export ICAN Fitness Data"))
                }
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Export / Backup Fitness Data (JSON)", color = WarmCream)
        }

        TextButton(
            onClick = { showResetConfirm = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset App & Re-run Onboarding", color = TerracottaAlert)
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
