package com.ican.fitness.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ican.fitness.data.local.BodyMeasurementEntity
import com.ican.fitness.data.local.PersonalRecordEntity
import com.ican.fitness.data.local.WorkoutSessionEntity
import com.ican.fitness.ui.theme.*
import java.time.LocalDate

@Composable
fun ProgressScreen(
    workouts: List<WorkoutSessionEntity>,
    measurements: List<BodyMeasurementEntity>,
    personalRecords: List<PersonalRecordEntity>,
    onSaveMeasurement: (BodyMeasurementEntity) -> Unit,
    onDeleteMeasurement: (BodyMeasurementEntity) -> Unit
) {
    var showLogDialog by remember { mutableStateOf(false) }
    var weightInput by remember { mutableStateOf("") }
    var waistInput by remember { mutableStateOf("") }
    var chestInput by remember { mutableStateOf("") }
    var armInput by remember { mutableStateOf("") }

    // Calculate Workout Streak (consecutive active days or scheduled training consistency)
    val uniqueDates = remember(workouts) {
        workouts.mapNotNull { runCatching { LocalDate.parse(it.dateStr) }.getOrNull() }
            .distinct()
            .sortedDescending()
    }
    val currentStreak = remember(uniqueDates) {
        if (uniqueDates.isEmpty()) 0
        else {
            var streak = 1
            for (i in 0 until uniqueDates.size - 1) {
                val diff = java.time.temporal.ChronoUnit.DAYS.between(uniqueDates[i + 1], uniqueDates[i])
                if (diff <= 2L) streak++ else break
            }
            streak
        }
    }

    val totalVolumeAllTime = workouts.sumOf { it.totalVolumeKg.toDouble() }.toInt()

    if (showLogDialog) {
        AlertDialog(
            onDismissRequest = { showLogDialog = false },
            containerColor = GraphiteSurface,
            title = { Text("Log Body Measurements", color = WarmCream) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = waistInput,
                        onValueChange = { waistInput = it },
                        label = { Text("Waist (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = chestInput,
                        onValueChange = { chestInput = it },
                        label = { Text("Chest (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = armInput,
                        onValueChange = { armInput = it },
                        label = { Text("Arms (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val entity = BodyMeasurementEntity(
                        dateStr = LocalDate.now().toString(),
                        weightKg = weightInput.toFloatOrNull(),
                        chestCm = chestInput.toFloatOrNull(),
                        waistCm = waistInput.toFloatOrNull(),
                        armCm = armInput.toFloatOrNull(),
                        hipCm = null,
                        thighCm = null
                    )
                    if (entity.weightKg != null || entity.waistCm != null || entity.chestCm != null || entity.armCm != null) {
                        onSaveMeasurement(entity)
                    }
                    weightInput = ""
                    waistInput = ""
                    chestInput = ""
                    armInput = ""
                    showLogDialog = false
                }) {
                    Text("Save Entry", color = OliveAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogDialog = false }) {
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
                Column {
                    Text("PERFORMANCE ANALYTICS", color = OliveAccent, style = MaterialTheme.typography.labelLarge)
                    Text("Progress & Records", color = WarmCream, style = MaterialTheme.typography.headlineLarge)
                }
                FilledTonalButton(onClick = { showLogDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Body")
                }
            }
        }

        // KEY METRICS ROW
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile("Streak", "$currentStreak days", Modifier.weight(1f))
                MetricTile("Workouts", "${workouts.size}", Modifier.weight(1f))
                MetricTile("Volume", "$totalVolumeAllTime kg", Modifier.weight(1f))
            }
        }

        // WEIGHT TREND CHART
        val weightPoints = measurements.mapNotNull { it.weightKg }.reversed()
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Body Weight Trend (kg)", color = WarmCream, style = MaterialTheme.typography.titleMedium)
                    if (weightPoints.size < 2) {
                        Text(
                            text = if (weightPoints.size == 1) "Latest weight: ${weightPoints.first()} kg. Log one more entry to plot trend line."
                            else "No weight entries logged yet. Tap 'Log Body' above to track.",
                            color = WarmCreamMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        val minW = weightPoints.min() - 1f
                        val maxW = weightPoints.max() + 1f
                        val range = (maxW - minW).coerceAtLeast(1f)

                        Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                            val stepX = size.width / (weightPoints.size - 1)
                            val path = Path()
                            weightPoints.forEachIndexed { idx, value ->
                                val x = idx * stepX
                                val normalized = (value - minW) / range
                                val y = size.height - (normalized * size.height * 0.85f + size.height * 0.08f)
                                if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                drawCircle(color = OliveAccent, radius = 6f, center = Offset(x, y))
                            }
                            drawPath(path = path, color = OliveAccent, style = Stroke(width = 4f, cap = StrokeCap.Round))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Start: ${weightPoints.first()} kg", color = WarmCreamMuted, style = MaterialTheme.typography.bodyMedium)
                            Text("Latest: ${weightPoints.last()} kg", color = OliveAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // PERSONAL RECORDS
        item {
            Text("Personal Records (${personalRecords.size})", color = WarmCream, style = MaterialTheme.typography.titleLarge)
        }
        if (personalRecords.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Complete workout sets to automatically unlock Personal Records.",
                        color = WarmCreamMuted,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(personalRecords, key = { it.exerciseName }) { pr ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(pr.exerciseName, color = WarmCream, fontWeight = FontWeight.SemiBold)
                            Text("Achieved: ${pr.achievedDateStr}", color = WarmCreamMuted, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            text = "${pr.maxWeightKg} kg × ${pr.maxReps} reps",
                            color = AmberHighlight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // BODY MEASUREMENTS LOG
        if (measurements.isNotEmpty()) {
            item {
                Text("Body Measurements History", color = WarmCream, style = MaterialTheme.typography.titleLarge)
            }
            items(measurements, key = { it.id }) { m ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(m.dateStr, color = OliveAccent, fontWeight = FontWeight.Bold)
                            val parts = listOfNotNull(
                                m.weightKg?.let { "Weight: ${it}kg" },
                                m.waistCm?.let { "Waist: ${it}cm" },
                                m.chestCm?.let { "Chest: ${it}cm" },
                                m.armCm?.let { "Arms: ${it}cm" }
                            )
                            Text(parts.joinToString(" • "), color = WarmCream, style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = { onDeleteMeasurement(m) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = WarmCreamMuted)
                        }
                    }
                }
            }
        }

        // RECENT WORKOUT HISTORY
        item {
            Text("Completed Workouts (${workouts.size})", color = WarmCream, style = MaterialTheme.typography.titleLarge)
        }
        items(workouts.take(15), key = { it.id }) { session ->
            Card(
                colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(session.title, color = WarmCream, fontWeight = FontWeight.SemiBold)
                        Text(session.dateStr, color = OliveAccent, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = "${session.durationSeconds / 60} min • ${session.totalSetsCompleted} sets • ${session.totalVolumeKg.toInt()} kg volume",
                        color = WarmCreamMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, color = WarmCreamMuted, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = OliveAccent, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}
