package com.ican.fitness.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ican.fitness.data.local.GoalEntity
import com.ican.fitness.data.remote.SourceReference
import com.ican.fitness.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    goals: List<GoalEntity>,
    isResearching: Boolean,
    errorMessage: String?,
    onSubmitGoalResearch: (String) -> Unit,
    onUpdateGoalStatus: (GoalEntity, String) -> Unit,
    onDeleteGoal: (GoalEntity) -> Unit
) {
    val context = LocalContext.current
    var goalInput by remember { mutableStateOf("") }
    var selectedGoalForDetail by remember { mutableStateOf<GoalEntity?>(null) }

    val quickSuggestions = listOf(
        "Reduce belly fat",
        "Reduce chest fat",
        "Build chest muscles",
        "Build bigger arms",
        "Increase stamina",
        "Gain healthy weight",
        "Improve flexibility"
    )

    if (selectedGoalForDetail != null) {
        val goal = selectedGoalForDetail!!
        val gson = Gson()
        val stringListType = object : TypeToken<List<String>>() {}.type
        val refListType = object : TypeToken<List<SourceReference>>() {}.type

        val facts: List<String> = runCatching { gson.fromJson<List<String>>(goal.scientificFactsJson, stringListType) }.getOrDefault(emptyList())
        val exercises: List<String> = runCatching { gson.fromJson<List<String>>(goal.recommendedExercisesJson, stringListType) }.getOrDefault(emptyList())
        val foodsEat: List<String> = runCatching { gson.fromJson<List<String>>(goal.foodsToEatJson, stringListType) }.getOrDefault(emptyList())
        val foodsLimit: List<String> = runCatching { gson.fromJson<List<String>>(goal.foodsToLimitJson, stringListType) }.getOrDefault(emptyList())
        val mistakes: List<String> = runCatching { gson.fromJson<List<String>>(goal.commonMistakesJson, stringListType) }.getOrDefault(emptyList())
        val safety: List<String> = runCatching { gson.fromJson<List<String>>(goal.safetyPrecautionsJson, stringListType) }.getOrDefault(emptyList())
        val refs: List<SourceReference> = runCatching { gson.fromJson<List<SourceReference>>(goal.referencesJson, refListType) }.getOrDefault(emptyList())

        ModalBottomSheet(
            onDismissRequest = { selectedGoalForDetail = null },
            containerColor = GraphiteSurface,
            contentColor = WarmCream
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(goal.title, style = MaterialTheme.typography.headlineMedium, color = WarmCream)
                Text(
                    text = if (goal.isFromOnlineResearch) "Source: Live Scientific Web Research + AI Synthesis" else "Source: Saved Offline Strategy",
                    color = OliveAccent,
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ACTIVE", "PAUSED", "COMPLETED").forEach { st ->
                        FilterChip(
                            selected = goal.status == st,
                            onClick = {
                                onUpdateGoalStatus(goal, st)
                                selectedGoalForDetail = goal.copy(status = st)
                            },
                            label = { Text(st) }
                        )
                    }
                }

                SectionBlock("Goal Explanation & Evidence", goal.goalExplanation)

                if (facts.isNotEmpty()) {
                    Text("Key Scientific Facts", color = OliveAccent, fontWeight = FontWeight.Bold)
                    facts.forEach { Text("• $it", color = WarmCream, style = MaterialTheme.typography.bodyMedium) }
                }

                if (exercises.isNotEmpty()) {
                    Text("Recommended Exercises", color = OliveAccent, fontWeight = FontWeight.Bold)
                    exercises.forEach { Text("• $it", color = WarmCream, style = MaterialTheme.typography.bodyMedium) }
                }

                SectionBlock("Weekly Workout Plan", goal.weeklyWorkoutPlan)
                SectionBlock("Sets & Repetitions", goal.setsAndReps)
                SectionBlock("Diet Recommendations", goal.dietRecommendations)

                if (foodsEat.isNotEmpty()) {
                    Text("Foods to Prioritize", color = OliveAccent, fontWeight = FontWeight.Bold)
                    foodsEat.forEach { Text("• $it", color = WarmCream, style = MaterialTheme.typography.bodyMedium) }
                }
                if (foodsLimit.isNotEmpty()) {
                    Text("Foods to Limit", color = AmberHighlight, fontWeight = FontWeight.Bold)
                    foodsLimit.forEach { Text("• $it", color = WarmCreamMuted, style = MaterialTheme.typography.bodyMedium) }
                }

                SectionBlock("Hydration Guidance", goal.hydration)
                SectionBlock("Recovery & Sleep", goal.recoveryAndSleep)
                SectionBlock("Expected Progress Timeline", goal.expectedProgressTimeline)

                if (mistakes.isNotEmpty()) {
                    Text("Common Mistakes", color = TerracottaAlert, fontWeight = FontWeight.Bold)
                    mistakes.forEach { Text("• $it", color = WarmCreamMuted, style = MaterialTheme.typography.bodyMedium) }
                }
                if (safety.isNotEmpty()) {
                    Text("Safety Precautions", color = AmberHighlight, fontWeight = FontWeight.Bold)
                    safety.forEach { Text("• $it", color = WarmCreamMuted, style = MaterialTheme.typography.bodyMedium) }
                }

                if (refs.isNotEmpty()) {
                    Text("Verified Research References (Tap to open)", color = OliveAccent, fontWeight = FontWeight.Bold)
                    refs.forEach { ref ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = GraphiteElevated),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    runCatching {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ref.url))
                                        context.startActivity(intent)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ref.title, color = WarmCream, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                    Text(ref.url, color = OliveAccent, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                }
                                Icon(Icons.Default.OpenInNew, contentDescription = "Open Link", tint = OliveAccent)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text("AI GOALS RESEARCH", color = OliveAccent, style = MaterialTheme.typography.labelLarge)
            Text("Evidence-Based Strategy", color = WarmCream, style = MaterialTheme.typography.headlineLarge)
            Text(
                text = "Enter any fitness goal. ICAN searches trusted sports science and medical sources online and builds a cited action plan.",
                color = WarmCreamMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = { goalInput = it },
                        label = { Text("Enter goal (e.g., Reduce chest fat, Build bigger arms)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(quickSuggestions) { suggestion ->
                            SuggestionChip(
                                onClick = { goalInput = suggestion },
                                label = { Text(suggestion) }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (goalInput.isNotBlank() && !isResearching) {
                                onSubmitGoalResearch(goalInput.trim())
                                goalInput = ""
                            }
                        },
                        enabled = goalInput.isNotBlank() && !isResearching,
                        colors = ButtonDefaults.buttonColors(containerColor = OliveAccent, contentColor = CharcoalBg),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isResearching) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CharcoalBg, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Researching Scientific Sources...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RESEARCH & BUILD PLAN", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (errorMessage != null) {
                        Text(errorMessage, color = TerracottaAlert, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        item {
            Text("Saved Research Plans (${goals.size})", color = WarmCream, style = MaterialTheme.typography.titleLarge)
        }

        if (goals.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No researched goals yet. Enter a goal above to search live scientific literature and save your personalized plan.",
                        color = WarmCreamMuted,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(goals, key = { it.id }) { goal ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedGoalForDetail = goal }
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(goal.status, color = OliveAccent, style = MaterialTheme.typography.labelLarge)
                                Text(goal.title, color = WarmCream, style = MaterialTheme.typography.titleLarge)
                            }
                            IconButton(onClick = { onDeleteGoal(goal) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Goal", tint = WarmCreamMuted)
                            }
                        }
                        Text(
                            text = goal.goalExplanation,
                            color = WarmCreamMuted,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3
                        )
                        Text("Tap to view full plan, diet & verified links →", color = OliveAccent, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun SectionBlock(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, color = OliveAccent, fontWeight = FontWeight.Bold)
        Text(body, color = WarmCream, style = MaterialTheme.typography.bodyMedium)
    }
}
