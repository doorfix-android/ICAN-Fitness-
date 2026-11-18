package com.ican.fitness.domain

import com.ican.fitness.data.local.UserProfile
import com.ican.fitness.data.local.WorkoutSessionEntity
import java.time.DayOfWeek
import java.time.LocalDate

data class GeneratedWorkoutPlan(
    val dateStr: String,
    val isRestDay: Boolean,
    val title: String,
    val focusMuscles: List<String>,
    val estimatedMinutes: Int,
    val exercises: List<ExerciseDefinition>,
    val recoveryAdvice: String,
    val motivationalQuote: String
)

object WorkoutGenerator {

    private val quotes = listOf(
        "Consistency beats intensity when intensity cannot be repeated.",
        "Train smart today so your body is stronger tomorrow.",
        "Small daily disciplines compound into undeniable strength.",
        "Respect recovery—muscles grow while you rest and refuel.",
        "You do not need perfection; you just need to show up."
    )

    fun isScheduledWorkoutDay(date: LocalDate, weeklyDaysCsv: String): Boolean {
        val code = when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "MON"
            DayOfWeek.TUESDAY -> "TUE"
            DayOfWeek.WEDNESDAY -> "WED"
            DayOfWeek.THURSDAY -> "THU"
            DayOfWeek.FRIDAY -> "FRI"
            DayOfWeek.SATURDAY -> "SAT"
            DayOfWeek.SUNDAY -> "SUN"
        }
        val activeDays = weeklyDaysCsv.split(",").map { it.trim().uppercase() }.filter { it.isNotEmpty() }
        return if (activeDays.isEmpty()) true else activeDays.contains(code)
    }

    fun generateDailyPlan(
        date: LocalDate,
        profile: UserProfile,
        recentSessions: List<WorkoutSessionEntity>,
        excludeExerciseIds: Set<String> = emptySet()
    ): GeneratedWorkoutPlan {
        val dateStr = date.toString()
        val quote = quotes[(date.dayOfYear) % quotes.size]
        val isWorkoutDay = isScheduledWorkoutDay(date, profile.weeklyWorkoutDays)

        // Recovery Intelligence: identify muscles trained in the last 48 hours
        val twoDaysAgoEpoch = System.currentTimeMillis() - (48L * 3600L * 1000L)
        val recentlyTrainedMuscles = recentSessions
            .filter { it.timestamp >= twoDaysAgoEpoch }
            .flatMap { it.musclesTrainedCsv.split(",") }
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .toSet()

        if (!isWorkoutDay) {
            val recoveryExercises = ExerciseLibrary.allExercises.filter {
                it.category == ExerciseCategory.STRETCHING || it.category == ExerciseCategory.MOBILITY
            }
            return GeneratedWorkoutPlan(
                dateStr = dateStr,
                isRestDay = true,
                title = "Active Recovery & Mobility Day",
                focusMuscles = listOf("Spine", "Hips", "Thoracic Mobility"),
                estimatedMinutes = 20,
                exercises = recoveryExercises,
                recoveryAdvice = "Prioritize 7.5–8.5 hours of sleep, light walking, and 10 minutes of gentle mobility to accelerate tissue repair.",
                motivationalQuote = quote
            )
        }

        val hasDumbbells = profile.availableEquipment.contains("Dumbbell", ignoreCase = true)
        val eligiblePool = ExerciseLibrary.allExercises.filter { ex ->
            ex.id !in excludeExerciseIds &&
            ex.category != ExerciseCategory.STRETCHING &&
            ex.category != ExerciseCategory.MOBILITY &&
            (ex.equipment == "Bodyweight" || (hasDumbbells && ex.equipment == "Dumbbells"))
        }

        // Prioritize exercises whose primary target muscle wasn't heavily taxed in the last 48h
        val freshExercises = eligiblePool.filter { ex ->
            ex.targetMuscles.none { muscle ->
                recentlyTrainedMuscles.any { recent -> muscle.lowercase().contains(recent) || recent.contains(muscle.lowercase()) }
            }
        }

        val candidatePool = if (freshExercises.size >= 4) freshExercises else eligiblePool
        val targetCount = when {
            profile.workoutDurationMinutes <= 30 -> 4
            profile.workoutDurationMinutes <= 50 -> 5
            else -> 6
        }

        // Deterministic daily rotation based on dayOfYear so Today stays consistent unless refreshed
        val rotated = candidatePool.indices.map { idx ->
            candidatePool[(idx + date.dayOfYear) % candidatePool.size]
        }.distinctBy { it.category }.take(targetCount).toMutableList()

        if (rotated.size < targetCount) {
            for (ex in candidatePool) {
                if (rotated.none { it.id == ex.id }) {
                    rotated.add(ex)
                }
                if (rotated.size >= targetCount) break
            }
        }

        val muscles = rotated.flatMap { it.targetMuscles }.distinct().take(4)
        val recoveryNote = if (recentlyTrainedMuscles.isNotEmpty()) {
            "Recovery Intelligence adjusted today's focus away from recently trained muscles (${recentlyTrainedMuscles.take(2).joinToString(", ")})."
        } else {
            "All major muscle groups are recovered. Focus on controlled eccentric tempo and full range of motion."
        }

        return GeneratedWorkoutPlan(
            dateStr = dateStr,
            isRestDay = false,
            title = "${profile.primaryGoal.take(24)} Session",
            focusMuscles = muscles,
            estimatedMinutes = profile.workoutDurationMinutes,
            exercises = rotated,
            recoveryAdvice = recoveryNote,
            motivationalQuote = quote
        )
    }
}
