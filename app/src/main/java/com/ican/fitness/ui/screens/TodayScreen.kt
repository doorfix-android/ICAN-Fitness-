package com.ican.fitness.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ican.fitness.data.local.*
import com.ican.fitness.data.remote.MealItem
import com.ican.fitness.domain.*
import com.ican.fitness.ui.components.ExerciseAnimationCanvas
import com.ican.fitness.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TodayScreen(
    profile: UserProfile,
    todayPlan: GeneratedWorkoutPlan,
    todayMealPlan: MealPlanEntity?,
    activeGoalsCount: Int,
    interruptedSession: WorkoutSessionEntity?,
    onStartWorkout: () -> Unit,
    onRefreshWorkout: () -> Unit,
    onAddWater: (Int) -> Unit,
    onNavigateToDiet: () -> Unit
) {
    val today = LocalDate.now()
    val formattedDate = today.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy"))
    val waterToday = if (profile.waterDate == today.toString()) profile.dailyWaterMl else 0

    var selectedExerciseForDetail by remember { mutableStateOf<ExerciseDefinition?>(null) }
    var selectedCategory by remember { mutableStateOf<ExerciseCategory?>(null) }

    if (selectedExerciseForDetail != null) {
        ExerciseDetailSheet(
            exercise = selectedExerciseForDetail!!,
            beginnerMode = profile.beginnerMode,
            onDismiss = { selectedExerciseForDetail = null }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = formattedDate.uppercase(), color = OliveAccent, style = MaterialTheme.typography.labelLarge)
            Text(
                text = "Welcome back, ${profile.name.ifBlank { "Athlete" }}",
                color = WarmCream,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "\"${todayPlan.motivationalQuote}\"",
                color = WarmCreamMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (interruptedSession != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = OliveContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Unfinished Workout Saved", color = WarmCream, fontWeight = FontWeight.Bold)
                            Text(interruptedSession.title, color = WarmCreamMuted, style = MaterialTheme.typography.bodyMedium)
                        }
                        Button(
                            onClick = onStartWorkout,
                            colors = ButtonDefaults.buttonColors(containerColor = OliveAccent, contentColor = CharcoalBg)
                        ) {
                            Text("Resume")
                        }
                    }
                }
            }
        }

        // TODAY'S WORKOUT OR REST DAY DASHBOARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (todayPlan.isRestDay) "TODAY • RECOVERY DAY" else "TODAY'S WORKOUT",
                                color = OliveAccent,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(todayPlan.title, color = WarmCream, style = MaterialTheme.typography.headlineMedium)
                            Text(
                                text = "${todayPlan.estimatedMinutes} min • Focus: ${todayPlan.focusMuscles.joinToString(", ")}",
                                color = WarmCreamMuted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(onClick = onRefreshWorkout) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Exercises", tint = WarmCreamMuted)
                        }
                    }

                    Text(todayPlan.recoveryAdvice, color = WarmCreamMuted, style = MaterialTheme.typography.bodyMedium)

                    todayPlan.exercises.forEach { exercise ->
                        Surface(
                            color = GraphiteElevated,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedExerciseForDetail = exercise }
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(exercise.name, color = WarmCream, fontWeight = FontWeight.SemiBold)
                                    Text("${exercise.defaultSets} × ${exercise.defaultReps}", color = OliveAccent, style = MaterialTheme.typography.bodyMedium)
                                }
                                Text(
                                    text = "Muscles: ${exercise.targetMuscles.joinToString(", ")} • Tap for form guide",
                                    color = WarmCreamMuted,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                ExerciseAnimationCanvas(pattern = exercise.animationPattern, exerciseName = exercise.name)
                            }
                        }
                    }

                    Button(
                        onClick = onStartWorkout,
                        colors = ButtonDefaults.buttonColors(containerColor = OliveAccent, contentColor = CharcoalBg),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (todayPlan.isRestDay) "START GUIDED MOBILITY SESSION" else "START TODAY'S WORKOUT",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // WATER INTAKE & DAILY NUTRITION SUMMARY
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalDrink, contentDescription = null, tint = OliveAccent)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hydration", color = WarmCream, fontWeight = FontWeight.SemiBold)
                        }
                        Text("${waterToday} / 3000 ml", color = OliveAccent, style = MaterialTheme.typography.titleLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(
                                onClick = { onAddWater(250) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("+250ml")
                            }
                            FilledTonalButton(
                                onClick = { onAddWater(500) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("+500ml")
                            }
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToDiet() }
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Daily Nutrition", color = WarmCream, fontWeight = FontWeight.SemiBold)
                        if (todayMealPlan != null) {
                            val listType = object : TypeToken<List<MealItem>>() {}.type
                            val meals: List<MealItem> = Gson().fromJson(todayMealPlan.mealsJson, listType)
                            val doneCount = todayMealPlan.completedMealTypesCsv.split(",").count { it.isNotBlank() }
                            Text("${todayMealPlan.targetCalories} kcal", color = OliveAccent, style = MaterialTheme.typography.titleLarge)
                            Text("$doneCount / ${meals.size} meals logged", color = WarmCreamMuted, style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Text("Tap to load plan", color = OliveAccent, style = MaterialTheme.typography.titleMedium)
                            Text("Active Goals: $activeGoalsCount", color = WarmCreamMuted, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        // INTERACTIVE 12-CATEGORY EXERCISE LIBRARY BROWSER
        item {
            Text("Exercise Library (12 Categories)", color = WarmCream, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All") }
                    )
                }
                items(ExerciseCategory.entries) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category.displayName) }
                    )
                }
            }
        }

        val filteredLibrary = if (selectedCategory == null) ExerciseLibrary.allExercises
        else ExerciseLibrary.allExercises.filter { it.category == selectedCategory }

        items(filteredLibrary, key = { "lib_${it.id}" }) { ex ->
            Card(
                colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedExerciseForDetail = ex }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ex.name, color = WarmCream, fontWeight = FontWeight.SemiBold)
                        Text("${ex.category.displayName} • ${ex.equipment} • ${ex.difficulty}", color = WarmCreamMuted, style = MaterialTheme.typography.bodyMedium)
                    }
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = OliveAccent)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
