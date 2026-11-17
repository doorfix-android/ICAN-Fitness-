package com.ican.fitness.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fitness_goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val status: String = "ACTIVE", // ACTIVE, PAUSED, COMPLETED
    val goalExplanation: String,
    val scientificFactsJson: String,
    val recommendedExercisesJson: String,
    val weeklyWorkoutPlan: String,
    val setsAndReps: String,
    val dietRecommendations: String,
    val foodsToEatJson: String,
    val foodsToLimitJson: String,
    val hydration: String,
    val recoveryAndSleep: String,
    val expectedProgressTimeline: String,
    val commonMistakesJson: String,
    val safetyPrecautionsJson: String,
    val referencesJson: String,
    val isFromOnlineResearch: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_meal_plans")
data class MealPlanEntity(
    @PrimaryKey val dateStr: String, // YYYY-MM-DD
    val targetCalories: Int,
    val hydrationGuidance: String,
    val mealsJson: String,
    val completedMealTypesCsv: String = "",
    val isOnlineGenerated: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateStr: String,
    val title: String,
    val durationSeconds: Long,
    val totalSetsCompleted: Int,
    val totalRepsCompleted: Int,
    val totalVolumeKg: Float,
    val musclesTrainedCsv: String,
    val exercisesSummaryJson: String,
    val isCompleted: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateStr: String,
    val weightKg: Float?,
    val chestCm: Float?,
    val waistCm: Float?,
    val armCm: Float?,
    val hipCm: Float?,
    val thighCm: Float?,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "personal_records")
data class PersonalRecordEntity(
    @PrimaryKey val exerciseName: String,
    val maxWeightKg: Float,
    val maxReps: Int,
    val achievedDateStr: String
)
