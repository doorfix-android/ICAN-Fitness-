package com.ican.fitness.data.remote

import com.ican.fitness.data.local.UserProfile

data class ResearchRequest(
    val goalQuery: String,
    val userProfile: UserProfile
)

data class SourceReference(
    val title: String,
    val url: String
)

data class ResearchResponse(
    val goalExplanation: String,
    val scientificFacts: List<String>,
    val recommendedExercises: List<String>,
    val weeklyWorkoutPlan: String,
    val setsAndReps: String,
    val dietRecommendations: String,
    val foodsToEat: List<String>,
    val foodsToLimit: List<String>,
    val hydration: String,
    val recoveryAndSleep: String,
    val expectedProgressTimeline: String,
    val commonMistakes: List<String>,
    val safetyPrecautions: List<String>,
    val references: List<SourceReference>?,
    val fetchedAt: String?
)

data class MealPlanRequest(
    val userProfile: UserProfile,
    val isWorkoutDay: Boolean,
    val focusNote: String
)

data class MealItem(
    val mealType: String,
    val foodName: String,
    val portionSize: String,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val preparationInstructions: String,
    val alternatives: List<String>
)

data class MealPlanResponse(
    val targetCalories: Int,
    val hydrationGuidance: String,
    val meals: List<MealItem>
)
