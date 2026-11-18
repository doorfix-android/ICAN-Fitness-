package com.ican.fitness.ui.screens

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    existingProfile: UserProfile,
    onComplete: (UserProfile) -> Unit
) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(0) }

    var name by remember { mutableStateOf(existingProfile.name) }
    var ageText by remember { mutableStateOf(existingProfile.age.toString()) }
    var heightText by remember { mutableStateOf(existingProfile.heightCm.toInt().toString()) }
    var weightText by remember { mutableStateOf(existingProfile.weightKg.toInt().toString()) }

    var experience by remember { mutableStateOf(existingProfile.fitnessExperience) }
    var primaryGoal by remember { mutableStateOf(existingProfile.primaryGoal) }
    var secondaryGoal by remember { mutableStateOf(existingProfile.secondaryGoal) }
    var targetMuscles by remember { mutableStateOf(existingProfile.targetMuscles) }

    var selectedEquipment by remember {
        mutableStateOf(
            existingProfile.availableEquipment.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        )
    }
    var durationMin by remember { mutableIntStateOf(existingProfile.workoutDurationMinutes) }
    var selectedDays by remember {
        mutableStateOf(
            existingProfile.weeklyWorkoutDays.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        )
    }

    var dietaryPref by remember { mutableStateOf(existingProfile.dietaryPreference) }
    var allergies by remember { mutableStateOf(existingProfile.foodAllergies) }
    var activityLevel by remember { mutableStateOf(existingProfile.activityLevel) }
    var alarmEnabled by remember { mutableStateOf(existingProfile.alarmEnabled) }
    var alarmHour by remember { mutableIntStateOf(existingProfile.alarmHour) }
    var alarmMinute by remember { mutableIntStateOf(existingProfile.alarmMinute) }

    Scaffold(
        containerColor = CharcoalBg,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step > 0) {
                    OutlinedButton(
                        onClick = { step-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Back", color = WarmCream)
                    }
                }
                Button(
                    onClick = {
                        if (step < 3) {
                            step++
                        } else {
                            val finalProfile = UserProfile(
                                onboardingCompleted = true,
                                name = name.trim().ifBlank { "Athlete" },
                                age = ageText.toIntOrNull()?.coerceIn(13, 95) ?: 25,
                                heightCm = heightText.toFloatOrNull()?.coerceIn(100f, 240f) ?: 170f,
                                weightKg = weightText.toFloatOrNull()?.coerceIn(30f, 250f) ?: 70f,
                                fitnessExperience = experience,
                                primaryGoal = primaryGoal,
                                secondaryGoal = secondaryGoal,
                                targetMuscles = targetMuscles,
                                availableEquipment = if (selectedEquipment.isEmpty()) "Bodyweight" else selectedEquipment.joinToString(", "),
                                workoutDurationMinutes = durationMin,
                                weeklyWorkoutDays = if (selectedDays.isEmpty()) "MON,WED,FRI" else selectedDays.joinToString(","),
                                dietaryPreference = dietaryPref,
                                foodAllergies = allergies.trim().ifBlank { "None" },
                                activityLevel = activityLevel,
                                alarmHour = alarmHour,
                                alarmMinute = alarmMinute,
                                alarmEnabled = alarmEnabled,
                                beginnerMode = experience == "Beginner"
                            )
                            if (alarmEnabled) {
                                AlarmScheduler.scheduleNextWorkoutAlarm(context, finalProfile)
                            } else {
                                AlarmScheduler.cancelAllWorkoutAlarms(context)
                            }
                            onComplete(finalProfile)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OliveAccent, contentColor = CharcoalBg),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (step < 3) "Continue" else "Complete Setup",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("ICAN", color = OliveAccent, style = MaterialTheme.typography.labelLarge)
            Text(
                text = when (step) {
                    0 -> "Personalize Your Coach"
                    1 -> "Training Goals & Experience"
                    2 -> "Schedule & Equipment"
                    else -> "Nutrition & Wake-Up Alarm"
                },
                color = WarmCream,
                style = MaterialTheme.typography.headlineMedium
            )
            LinearProgressIndicator(
                progress = { (step + 1) / 4f },
                modifier = Modifier.fillMaxWidth(),
                color = OliveAccent,
                trackColor = GraphiteElevated
            )

            when (step) {
                0 -> {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Your Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it },
                        label = { Text("Age (years)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Weight (kg) — Optional") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                1 -> {
                    Text("Fitness Experience", color = WarmCream, style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Beginner", "Intermediate", "Advanced").forEach { level ->
                            FilterChip(
                                selected = experience == level,
                                onClick = { experience = level },
                                label = { Text(level) }
                            )
                        }
                    }

                    Text("Primary Goal", color = WarmCream, style = MaterialTheme.typography.titleMedium)
                    val goals = listOf(
                        "Build Strength & Lean Muscle",
                        "Lose Overall Body Fat",
                        "Increase Stamina & Endurance",
                        "Gain Healthy Weight",
                        "Improve Mobility & Posture"
                    )
                    goals.forEach { g ->
                        FilterChip(
                            selected = primaryGoal == g,
                            onClick = { primaryGoal = g },
                            label = { Text(g) }
                        )
                    }

                    OutlinedTextField(
                        value = secondaryGoal,
                        onValueChange = { secondaryGoal = it },
                        label = { Text("Secondary Goal (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = targetMuscles,
                        onValueChange = { targetMuscles = it },
                        label = { Text("Target Muscle Groups") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                2 -> {
                    Text("Available Equipment", color = WarmCream, style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Bodyweight", "Dumbbells", "Resistance Bands", "Full Gym").forEach { eq ->
                            val isSelected = selectedEquipment.contains(eq)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedEquipment = if (isSelected) selectedEquipment - eq else selectedEquipment + eq
                                },
                                label = { Text(eq) }
                            )
                        }
                    }

                    Text("Preferred Workout Duration", color = WarmCream, style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(25, 35, 45, 60).forEach { mins ->
                            FilterChip(
                                selected = durationMin == mins,
                                onClick = { durationMin = mins },
                                label = { Text("$mins min") }
                            )
                        }
                    }

                    Text("Weekly Workout Days", color = WarmCream, style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach { day ->
                            val isSelected = selectedDays.contains(day)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                                },
                                label = { Text(day) }
                            )
                        }
                    }
                }

                3 -> {
                    Text("Dietary Preference", color = WarmCream, style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Vegetarian", "Eggetarian", "Non-Vegetarian", "Vegan").forEach { diet ->
                            FilterChip(
                                selected = dietaryPref == diet,
                                onClick = { dietaryPref = diet },
                                label = { Text(diet) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = allergies,
                        onValueChange = { allergies = it },
                        label = { Text("Food Allergies / Restrictions (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Daily Activity Level", color = WarmCream, style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active").forEach { act ->
                            FilterChip(
                                selected = activityLevel == act,
                                onClick = { activityLevel = act },
                                label = { Text(act) }
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Full-Screen Gym Alarm", color = WarmCream, fontWeight = FontWeight.SemiBold)
                                Switch(checked = alarmEnabled, onCheckedChange = { alarmEnabled = it })
                            }
                            if (alarmEnabled) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = alarmHour.toString().padStart(2, '0'),
                                        onValueChange = { alarmHour = it.toIntOrNull()?.coerceIn(0, 23) ?: alarmHour },
                                        label = { Text("Hour (0-23)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = alarmMinute.toString().padStart(2, '0'),
                                        onValueChange = { alarmMinute = it.toIntOrNull()?.coerceIn(0, 59) ?: alarmMinute },
                                        label = { Text("Minute (0-59)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
