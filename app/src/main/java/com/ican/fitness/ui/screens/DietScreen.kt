package com.ican.fitness.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ican.fitness.data.local.MealPlanEntity
import com.ican.fitness.data.local.UserProfile
import com.ican.fitness.data.remote.MealItem
import com.ican.fitness.ui.theme.*
import java.time.LocalDate

@Composable
fun DietScreen(
    profile: UserProfile,
    activeMealPlan: MealPlanEntity?,
    recentPlans: List<MealPlanEntity>,
    isGenerating: Boolean,
    onGenerateMealPlan: (String) -> Unit,
    onToggleMealCompleted: (MealPlanEntity, String) -> Unit,
    onReplaceMeal: (MealPlanEntity, String, String) -> Unit,
    onAddWater: (Int) -> Unit
) {
    val todayStr = LocalDate.now().toString()
    var selectedDateStr by remember { mutableStateOf(todayStr) }
    var swapDialogMeal by remember { mutableStateOf<MealItem?>(null) }

    val displayedPlan = remember(selectedDateStr, activeMealPlan, recentPlans) {
        if (selectedDateStr == todayStr) activeMealPlan
        else recentPlans.find { it.dateStr == selectedDateStr } ?: activeMealPlan
    }

    LaunchedEffect(Unit) {
        if (activeMealPlan == null && !isGenerating) {
            onGenerateMealPlan(todayStr)
        }
    }

    if (swapDialogMeal != null && displayedPlan != null) {
        val meal = swapDialogMeal!!
        AlertDialog(
            onDismissRequest = { swapDialogMeal = null },
            containerColor = GraphiteSurface,
            title = { Text("Swap ${meal.mealType}", color = WarmCream) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Choose a nutritious alternative:", color = WarmCreamMuted)
                    meal.alternatives.forEach { alt ->
                        FilledTonalButton(
                            onClick = {
                                onReplaceMeal(displayedPlan, meal.mealType, alt)
                                swapDialogMeal = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(alt)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { swapDialogMeal = null }) {
                    Text("Cancel", color = WarmCreamMuted)
                }
            }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("DAILY NUTRITION", color = OliveAccent, style = MaterialTheme.typography.labelLarge)
                    Text("${profile.dietaryPreference} Plan", color = WarmCream, style = MaterialTheme.typography.headlineLarge)
                }
                Button(
                    onClick = { onGenerateMealPlan(selectedDateStr) },
                    enabled = !isGenerating,
                    colors = ButtonDefaults.buttonColors(containerColor = OliveAccent, contentColor = CharcoalBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = CharcoalBg, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Meal Plan")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Refresh", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (recentPlans.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(recentPlans) { planItem ->
                        FilterChip(
                            selected = selectedDateStr == planItem.dateStr,
                            onClick = { selectedDateStr = planItem.dateStr },
                            label = { Text(if (planItem.dateStr == todayStr) "Today" else planItem.dateStr) }
                        )
                    }
                }
            }
        }

        if (displayedPlan == null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Building your personalized Indian & whole-food meal plan...", color = WarmCream)
                    }
                }
            }
        } else {
            val listType = object : TypeToken<List<MealItem>>() {}.type
            val meals: List<MealItem> = runCatching { Gson().fromJson<List<MealItem>>(displayedPlan.mealsJson, listType) }.getOrDefault(emptyList())
            val completedTypes = displayedPlan.completedMealTypesCsv.split(",").filter { it.isNotBlank() }.toSet()

            val loggedCalories = meals.filter { completedTypes.contains(it.mealType) }.sumOf { it.calories }
            val totalProtein = meals.sumOf { it.proteinGrams }
            val totalCarbs = meals.sumOf { it.carbsGrams }
            val totalFat = meals.sumOf { it.fatGrams }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (displayedPlan.isOnlineGenerated) "AI Personalized Plan (Estimated Macros)" else "Offline Cached Indian Nutrition Plan (Estimated Macros)",
                            color = OliveAccent,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "Logged: $loggedCalories / ~${displayedPlan.targetCalories} kcal",
                            color = WarmCream,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Daily Totals: ~${totalProtein}g Protein • ~${totalCarbs}g Carbs • ~${totalFat}g Fat",
                            color = WarmCreamMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Hydration: ${displayedPlan.hydrationGuidance}",
                            color = WarmCreamMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(onClick = { onAddWater(250) }) { Text("+250 ml Water") }
                            FilledTonalButton(onClick = { onAddWater(500) }) { Text("+500 ml Water") }
                        }
                    }
                }
            }

            items(meals, key = { "${displayedPlan.dateStr}_${it.mealType}" }) { meal ->
                val isDone = completedTypes.contains(meal.mealType)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDone) OliveContainer else GraphiteSurface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(meal.mealType.uppercase(), color = OliveAccent, style = MaterialTheme.typography.labelLarge)
                                Text(meal.foodName, color = WarmCream, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("Portion: ${meal.portionSize}", color = WarmCreamMuted, style = MaterialTheme.typography.bodyMedium)
                            }
                            Row {
                                if (meal.alternatives.isNotEmpty()) {
                                    IconButton(onClick = { swapDialogMeal = meal }) {
                                        Icon(Icons.Default.SwapHoriz, contentDescription = "Replace Meal", tint = WarmCreamMuted)
                                    }
                                }
                                IconButton(onClick = { onToggleMealCompleted(displayedPlan, meal.mealType) }) {
                                    Icon(
                                        imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Mark Meal Completed",
                                        tint = if (isDone) OliveAccent else WarmCreamMuted
                                    )
                                }
                            }
                        }

                        Text(
                            text = "~${meal.calories} kcal • P: ${meal.proteinGrams}g • C: ${meal.carbsGrams}g • F: ${meal.fatGrams}g",
                            color = WarmCream,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Prep: ${meal.preparationInstructions}",
                            color = WarmCreamMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
