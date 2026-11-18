package com.ican.fitness.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ican.fitness.data.local.WorkoutSessionEntity
import com.ican.fitness.domain.ExerciseDefinition
import com.ican.fitness.domain.ExerciseLibrary
import com.ican.fitness.domain.GeneratedWorkoutPlan
import com.ican.fitness.ui.components.ExerciseAnimationCanvas
import com.ican.fitness.ui.theme.*
import kotlinx.coroutines.delay

data class SetEntryState(
    val setNumber: Int,
    var repsText: String,
    var weightText: String,
    var completed: Boolean = false
)

@Composable
fun WorkoutSessionScreen(
    plan: GeneratedWorkoutPlan,
    beginnerMode: Boolean,
    onFinishWorkout: (WorkoutSessionEntity, List<Triple<String, Float, Int>>, List<String>) -> Unit,
    onSaveInterrupted: (WorkoutSessionEntity) -> Unit,
    onCancelWorkout: () -> Unit
) {
    var exercises by remember { mutableStateOf(plan.exercises) }
    val setStates = remember {
        mutableStateMapOf<String, MutableList<SetEntryState>>().apply {
            plan.exercises.forEach { ex ->
                put(
                    ex.id,
                    MutableList(ex.defaultSets) { idx ->
                        SetEntryState(setNumber = idx + 1, repsText = "12", weightText = "0")
                    }
                )
            }
        }
    }

    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var isPaused by remember { mutableStateOf(false) }
    var restSecondsRemaining by remember { mutableIntStateOf(0) }
    var showCancelConfirm by remember { mutableStateOf(false) }
    var completedSummary by remember { mutableStateOf<Pair<WorkoutSessionEntity, List<String>>?>(null) }

    LaunchedEffect(isPaused) {
        while (!isPaused && completedSummary == null) {
            delay(1000L)
            elapsedSeconds++
            if (restSecondsRemaining > 0) restSecondsRemaining--
        }
    }

    if (completedSummary != null) {
        val (summarySession, newPrs) = completedSummary!!
        Surface(modifier = Modifier.fillMaxSize(), color = CharcoalBg) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("WORKOUT COMPLETE", color = OliveAccent, style = MaterialTheme.typography.labelLarge)
                Text(summarySession.title, color = WarmCream, style = MaterialTheme.typography.headlineMedium)

                Card(
                    colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Duration: ${summarySession.durationSeconds / 60}m ${summarySession.durationSeconds % 60}s", color = WarmCream)
                        Text("Sets Completed: ${summarySession.totalSetsCompleted}", color = WarmCream)
                        Text("Total Reps: ${summarySession.totalRepsCompleted}", color = WarmCream)
                        Text("Total Volume Lifted: ${summarySession.totalVolumeKg.toInt()} kg", color = WarmCream)
                        Text("Muscles Trained: ${summarySession.musclesTrainedCsv}", color = OliveAccent)
                    }
                }

                if (newPrs.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = OliveContainer),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("NEW PERSONAL RECORDS!", color = AmberHighlight, fontWeight = FontWeight.Bold)
                            newPrs.forEach { pr ->
                                Text("• $pr", color = WarmCream)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onCancelWorkout,
                    colors = ButtonDefaults.buttonColors(containerColor = OliveAccent, contentColor = CharcoalBg),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Back to Today Dashboard", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    if (showCancelConfirm) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            containerColor = GraphiteSurface,
            title = { Text("Leave Workout?", color = WarmCream) },
            text = { Text("Save progress to resume later, or discard this session?", color = WarmCreamMuted) },
            confirmButton = {
                TextButton(onClick = {
                    val partial = buildSessionEntity(plan, exercises, setStates, elapsedSeconds, isCompleted = false)
                    onSaveInterrupted(partial)
                    onCancelWorkout()
                }) {
                    Text("Save & Pause", color = OliveAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCancelConfirm = false
                    onCancelWorkout()
                }) {
                    Text("Discard", color = TerracottaAlert)
                }
            }
        )
    }

    Scaffold(
        containerColor = CharcoalBg,
        topBar = {
            Surface(color = GraphiteSurface) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(plan.title, color = WarmCream, style = MaterialTheme.typography.titleLarge)
                            val mins = elapsedSeconds / 60
                            val secs = elapsedSeconds % 60
                            Text(
                                text = "Elapsed: %02d:%02d".format(mins, secs),
                                color = OliveAccent,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { isPaused = !isPaused }) {
                                Text(if (isPaused) "Resume" else "Pause", color = WarmCream)
                            }
                            TextButton(onClick = { showCancelConfirm = true }) {
                                Text("Exit", color = TerracottaAlert)
                            }
                        }
                    }
                    if (restSecondsRemaining > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = OliveContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Rest Timer: ${restSecondsRemaining}s", color = WarmCream, fontWeight = FontWeight.Bold)
                                TextButton(onClick = { restSecondsRemaining = 0 }) {
                                    Text("Skip Rest", color = OliveAccent)
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Button(
                onClick = {
                    val entity = buildSessionEntity(plan, exercises, setStates, elapsedSeconds, isCompleted = true)
                    val recordsToCheck = mutableListOf<Triple<String, Float, Int>>()
                    exercises.forEach { ex ->
                        setStates[ex.id]?.filter { it.completed }?.forEach { st ->
                            val w = st.weightText.toFloatOrNull() ?: 0f
                            val r = st.repsText.toIntOrNull() ?: 0
                            recordsToCheck.add(Triple(ex.name, w, r))
                        }
                    }
                    val prNames = recordsToCheck.filter { it.second > 0f || it.third >= 15 }.map { "${it.first} (${it.second}kg × ${it.third})" }.distinct()
                    onFinishWorkout(entity, recordsToCheck, prNames)
                    completedSummary = entity to prNames
                },
                colors = ButtonDefaults.buttonColors(containerColor = OliveAccent, contentColor = CharcoalBg),
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("FINISH WORKOUT", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            itemsIndexed(exercises, key = { _, ex -> ex.id }) { index, exercise ->
                val sets = setStates[exercise.id] ?: mutableListOf()
                Card(
                    colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(exercise.name, color = WarmCream, style = MaterialTheme.typography.titleMedium)
                                Text(exercise.targetMuscles.joinToString(", "), color = OliveAccent, style = MaterialTheme.typography.bodyMedium)
                            }
                            TextButton(onClick = {
                                val altId = exercise.alternativeExerciseIds.firstOrNull()
                                val replacement = altId?.let { ExerciseLibrary.findById(it) }
                                    ?: ExerciseLibrary.allExercises.firstOrNull { it.category == exercise.category && it.id != exercise.id }
                                if (replacement != null) {
                                    exercises = exercises.toMutableList().apply { set(index, replacement) }
                                    setStates[replacement.id] = MutableList(replacement.defaultSets) { idx ->
                                        SetEntryState(idx + 1, "12", "0")
                                    }
                                }
                            }) {
                                Text("Replace", color = WarmCreamMuted)
                            }
                        }

                        ExerciseAnimationCanvas(pattern = exercise.animationPattern, exerciseName = exercise.name)

                        if (beginnerMode) {
                            Text("Beginner Tip: ${exercise.beginnerModification}", color = WarmCreamMuted, style = MaterialTheme.typography.bodyMedium)
                        }

                        sets.forEachIndexed { sIdx, setItem ->
                            var reps by remember(exercise.id, sIdx) { mutableStateOf(setItem.repsText) }
                            var weight by remember(exercise.id, sIdx) { mutableStateOf(setItem.weightText) }
                            var done by remember(exercise.id, sIdx) { mutableStateOf(setItem.completed) }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Set ${setItem.setNumber}", color = WarmCream, modifier = Modifier.width(48.dp))
                                OutlinedTextField(
                                    value = weight,
                                    onValueChange = {
                                        weight = it
                                        setItem.weightText = it
                                    },
                                    label = { Text("kg") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = reps,
                                    onValueChange = {
                                        reps = it
                                        setItem.repsText = it
                                    },
                                    label = { Text("Reps") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                Checkbox(
                                    checked = done,
                                    onCheckedChange = { checked ->
                                        done = checked
                                        setItem.completed = checked
                                        if (checked) {
                                            restSecondsRemaining = exercise.restSeconds
                                        }
                                    }
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(onClick = {
                                val nextNum = sets.size + 1
                                val updated = sets.toMutableList().apply { add(SetEntryState(nextNum, "12", "0")) }
                                setStates[exercise.id] = updated
                            }) {
                                Text("+ Add Set", color = OliveAccent)
                            }
                            if (sets.size > 1) {
                                TextButton(onClick = {
                                    val updated = sets.toMutableList().apply { removeAt(lastIndex) }
                                    setStates[exercise.id] = updated
                                }) {
                                    Text("Skip Last Set", color = WarmCreamMuted)
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

private fun buildSessionEntity(
    plan: GeneratedWorkoutPlan,
    exercises: List<ExerciseDefinition>,
    setStates: Map<String, List<SetEntryState>>,
    elapsedSeconds: Long,
    isCompleted: Boolean
): WorkoutSessionEntity {
    var totalSets = 0
    var totalReps = 0
    var totalVolume = 0f

    exercises.forEach { ex ->
        setStates[ex.id]?.filter { it.completed }?.forEach { st ->
            totalSets++
            val r = st.repsText.toIntOrNull() ?: 0
            val w = st.weightText.toFloatOrNull() ?: 0f
            totalReps += r
            totalVolume += (r * w)
        }
    }

    return WorkoutSessionEntity(
        dateStr = plan.dateStr,
        title = plan.title,
        durationSeconds = elapsedSeconds,
        totalSetsCompleted = totalSets,
        totalRepsCompleted = totalReps,
        totalVolumeKg = totalVolume,
        musclesTrainedCsv = exercises.flatMap { it.targetMuscles }.distinct().joinToString(", "),
        exercisesSummaryJson = exercises.joinToString(", ") { it.name },
        isCompleted = isCompleted
    )
}
