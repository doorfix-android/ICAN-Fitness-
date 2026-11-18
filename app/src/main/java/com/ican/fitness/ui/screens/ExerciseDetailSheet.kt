package com.ican.fitness.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ican.fitness.domain.ExerciseDefinition
import com.ican.fitness.ui.components.ExerciseAnimationCanvas
import com.ican.fitness.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailSheet(
    exercise: ExerciseDefinition,
    beginnerMode: Boolean,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
            Text(exercise.name, style = MaterialTheme.typography.headlineMedium, color = WarmCream)
            Text(
                text = "${exercise.category.displayName} • ${exercise.equipment} • ${exercise.difficulty}",
                style = MaterialTheme.typography.bodyMedium,
                color = OliveAccent
            )

            ExerciseAnimationCanvas(
                pattern = exercise.animationPattern,
                exerciseName = exercise.name
            )

            Text(
                text = "Target Muscles: ${exercise.targetMuscles.joinToString(", ")}",
                style = MaterialTheme.typography.titleMedium,
                color = WarmCream
            )

            if (beginnerMode) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = OliveContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Beginner Variation", color = OliveAccent, fontWeight = FontWeight.Bold)
                        Text(exercise.beginnerModification, color = WarmCream, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Text("Step-by-Step Form", color = OliveAccent, fontWeight = FontWeight.Bold)
            exercise.instructions.forEachIndexed { idx, step ->
                Text("${idx + 1}. $step", color = WarmCream, style = MaterialTheme.typography.bodyLarge)
            }

            Text("Breathing Technique", color = OliveAccent, fontWeight = FontWeight.Bold)
            Text(exercise.breathingTechnique, color = WarmCreamMuted, style = MaterialTheme.typography.bodyMedium)

            Text("Common Mistakes to Avoid", color = TerracottaAlert, fontWeight = FontWeight.Bold)
            exercise.commonMistakes.forEach { mistake ->
                Text("• $mistake", color = WarmCreamMuted, style = MaterialTheme.typography.bodyMedium)
            }

            Text("Safety Precautions", color = AmberHighlight, fontWeight = FontWeight.Bold)
            Text(exercise.safetyPrecautions, color = WarmCreamMuted, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
