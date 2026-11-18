package com.ican.fitness.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ican.fitness.data.local.*
import com.ican.fitness.data.remote.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class IcanRepository(
    private val database: IcanDatabase,
    private val preferences: UserPreferencesRepository,
    private val apiService: IcanApiService
) {
    private val gson = Gson()

    val userProfile: Flow<UserProfile> = preferences.userProfileFlow
    val allGoals: Flow<List<GoalEntity>> = database.goalDao().getAllGoals()
    val completedWorkouts: Flow<List<WorkoutSessionEntity>> = database.workoutDao().getCompletedWorkouts()
    val personalRecords: Flow<List<PersonalRecordEntity>> = database.workoutDao().getAllPersonalRecords()
    val measurements: Flow<List<BodyMeasurementEntity>> = database.measurementDao().getAllMeasurements()
    val recentMealPlans: Flow<List<MealPlanEntity>> = database.mealPlanDao().getRecentMealPlans()

    fun getMealPlanForDate(dateStr: String): Flow<MealPlanEntity?> =
        database.mealPlanDao().getMealPlanForDate(dateStr)

    suspend fun saveUserProfile(profile: UserProfile) {
        preferences.saveProfile(profile)
    }

    suspend fun addWater(amountMl: Int) {
        val today = LocalDate.now().toString()
        val current = preferences.userProfileFlow.first()
        val base = if (current.waterDate == today) current.dailyWaterMl else 0
        preferences.updateWaterIntake(today, base + amountMl)
    }

    suspend fun researchAndSaveGoal(goalQuery: String): Result<GoalEntity> {
        return try {
            val profile = preferences.userProfileFlow.first()
            val response = apiService.researchGoal(ResearchRequest(goalQuery, profile))
            val entity = GoalEntity(
                title = goalQuery.trim(),
                status = "ACTIVE",
                goalExplanation = response.goalExplanation,
                scientificFactsJson = gson.toJson(response.scientificFacts),
                recommendedExercisesJson = gson.toJson(response.recommendedExercises),
                weeklyWorkoutPlan = response.weeklyWorkoutPlan,
                setsAndReps = response.setsAndReps,
                dietRecommendations = response.dietRecommendations,
                foodsToEatJson = gson.toJson(response.foodsToEat),
                foodsToLimitJson = gson.toJson(response.foodsToLimit),
                hydration = response.hydration,
                recoveryAndSleep = response.recoveryAndSleep,
                expectedProgressTimeline = response.expectedProgressTimeline,
                commonMistakesJson = gson.toJson(response.commonMistakes),
                safetyPrecautionsJson = gson.toJson(response.safetyPrecautions),
                referencesJson = gson.toJson(response.references ?: emptyList<SourceReference>()),
                isFromOnlineResearch = true
            )
            val insertedId = database.goalDao().insertGoal(entity)
            Result.success(entity.copy(id = insertedId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGoal(goal: GoalEntity) = database.goalDao().updateGoal(goal)
    suspend fun deleteGoal(goal: GoalEntity) = database.goalDao().deleteGoal(goal)

    suspend fun generateOrRefreshMealPlan(dateStr: String, isWorkoutDay: Boolean, focusNote: String = ""): Result<MealPlanEntity> {
        val profile = preferences.userProfileFlow.first()
        return try {
            val response = apiService.generateMealPlan(
                MealPlanRequest(
                    userProfile = profile,
                    isWorkoutDay = isWorkoutDay,
                    focusNote = focusNote
                )
            )
            val entity = MealPlanEntity(
                dateStr = dateStr,
                targetCalories = response.targetCalories,
                hydrationGuidance = response.hydrationGuidance,
                mealsJson = gson.toJson(response.meals),
                isOnlineGenerated = true
            )
            database.mealPlanDao().upsertMealPlan(entity)
            Result.success(entity)
        } catch (e: Exception) {
            // Responsible offline Indian-first fallback plan clearly labeled as offline cached guidance
            val fallbackMeals = buildOfflineIndianFallbackMeals(profile.dietaryPreference)
            val entity = MealPlanEntity(
                dateStr = dateStr,
                targetCalories = fallbackMeals.sumOf { it.calories },
                hydrationGuidance = "Drink 2.5 to 3.2 litres of water across the day (more on humid or heavy training days).",
                mealsJson = gson.toJson(fallbackMeals),
                isOnlineGenerated = false
            )
            database.mealPlanDao().upsertMealPlan(entity)
            Result.success(entity)
        }
    }

    suspend fun toggleMealCompleted(plan: MealPlanEntity, mealType: String) {
        val currentSet = plan.completedMealTypesCsv.split(",").filter { it.isNotBlank() }.toMutableSet()
        if (currentSet.contains(mealType)) currentSet.remove(mealType) else currentSet.add(mealType)
        database.mealPlanDao().upsertMealPlan(
            plan.copy(completedMealTypesCsv = currentSet.joinToString(","))
        )
    }

    suspend fun replaceMealInPlan(plan: MealPlanEntity, mealType: String, newFoodName: String) {
        val listType = object : TypeToken<List<MealItem>>() {}.type
        val meals: List<MealItem> = gson.fromJson(plan.mealsJson, listType)
        val updated = meals.map {
            if (it.mealType == mealType) it.copy(foodName = newFoodName) else it
        }
        database.mealPlanDao().upsertMealPlan(plan.copy(mealsJson = gson.toJson(updated)))
    }

    suspend fun saveWorkoutSession(session: WorkoutSessionEntity) {
        database.workoutDao().upsertSession(session)
    }

    suspend fun getInterruptedSession(): WorkoutSessionEntity? =
        database.workoutDao().getInterruptedSession()

    suspend fun deleteSessionById(id: Long) =
        database.workoutDao().deleteSessionById(id)

    suspend fun checkAndSavePersonalRecord(exerciseName: String, weightKg: Float, reps: Int, dateStr: String): Boolean {
        val existing = database.workoutDao().getRecordForExercise(exerciseName)
        val isNewPr = existing == null || weightKg > existing.maxWeightKg || (weightKg == existing.maxWeightKg && reps > existing.maxReps)
        if (isNewPr && (weightKg > 0f || reps > 0)) {
            database.workoutDao().upsertRecord(
                PersonalRecordEntity(
                    exerciseName = exerciseName,
                    maxWeightKg = maxOf(weightKg, existing?.maxWeightKg ?: 0f),
                    maxReps = maxOf(reps, existing?.maxReps ?: 0),
                    achievedDateStr = dateStr
                )
            )
        }
        return isNewPr
    }

    suspend fun saveMeasurement(measurement: BodyMeasurementEntity) =
        database.measurementDao().upsertMeasurement(measurement)

    suspend fun deleteMeasurement(measurement: BodyMeasurementEntity) =
        database.measurementDao().deleteMeasurement(measurement)

    suspend fun exportAllUserDataJson(): String {
        val profile = preferences.userProfileFlow.first()
        val goals = database.goalDao().getAllGoals().first()
        val workouts = database.workoutDao().getCompletedWorkouts().first()
        val body = database.measurementDao().getAllMeasurements().first()
        return gson.toJson(
            mapOf(
                "exportedAt" to LocalDate.now().toString(),
                "profile" to profile,
                "goals" to goals,
                "workouts" to workouts,
                "measurements" to body
            )
        )
    }

    suspend fun wipeAllUserData() {
        database.clearAllTables()
        preferences.clearAllPreferences()
    }

    private fun buildOfflineIndianFallbackMeals(dietPref: String): List<MealItem> {
        val isVeg = dietPref.equals("Vegetarian", true) || dietPref.equals("Vegan", true)
        val isVegan = dietPref.equals("Vegan", true)
        val isEgg = dietPref.equals("Eggetarian", true)

        return listOf(
            MealItem(
                mealType = "Breakfast",
                foodName = if (isVeg) "3 Steamed Idlis with Sambar & Peanut Chutney" else "3 Egg Bhurji with 2 Multigrain Chapatis",
                portionSize = "1 standard plate (~320g)",
                calories = 430,
                proteinGrams = if (isVeg) 15 else 24,
                carbsGrams = 62,
                fatGrams = 12,
                preparationInstructions = if (isVeg) "Steam fermented rice-urad batter idlis; serve hot with lentil-vegetable sambar." else "Scramble whole eggs with onion, tomato, turmeric, and minimal cold-pressed oil.",
                alternatives = listOf("Vegetable Rava Upma with Roasted Peanuts", "2 Besan Chillas with Mint Chutney")
            ),
            MealItem(
                mealType = "Morning Snack",
                foodName = "1 Medium Banana & 10 Soaked Almonds",
                portionSize = "1 banana + 15g almonds",
                calories = 195,
                proteinGrams = 5,
                carbsGrams = 29,
                fatGrams = 8,
                preparationInstructions = "Peel soaked almonds for easier digestion and pair with fresh banana.",
                alternatives = listOf("1 Guava with Roasted Chana", "1 Apple with 1 tbsp Peanut Butter")
            ),
            MealItem(
                mealType = "Lunch",
                foodName = when {
                    isVegan -> "Brown/Steamed Rice, Dal Tadka, Bhindi Sabzi & Cucumber Salad"
                    isVeg || isEgg -> "2 Phulkas, 1 Bowl Dal Tadka, 100g Paneer Bhurji & Curd"
                    else -> "1 Bowl Rice, 1 Chapati, 130g Grilled Chicken Curry & Dal"
                },
                portionSize = "1 balanced thali (~480g)",
                calories = 620,
                proteinGrams = if (isVegan) 22 else 36,
                carbsGrams = 72,
                fatGrams = 18,
                preparationInstructions = "Cook with moderate spices and 1–2 tsp cooking oil; include fresh raw salad for fibre.",
                alternatives = listOf("Rajma Chawal with Cucumber Raita", "Sambar Rice with Roasted Papad & Stir-Fried Beans")
            ),
            MealItem(
                mealType = "Evening Snack",
                foodName = if (isVegan) "Roasted Makhana & Masala Chaas (Spiced Soy Buttermilk)" else "Sprouted Moong Chaat with Lemon & Curd/Buttermilk",
                portionSize = "1 medium bowl (~180g)",
                calories = 210,
                proteinGrams = 12,
                carbsGrams = 30,
                fatGrams = 4,
                preparationInstructions = "Toss steamed green moong sprouts with chopped onion, tomato, coriander, and lemon juice.",
                alternatives = listOf("1 Bowl Roasted Chana & Green Tea", "2 Ragi Crackers with Hummus")
            ),
            MealItem(
                mealType = "Dinner",
                foodName = when {
                    isVegan -> "2 Ragi Chapatis with Tofu & Mixed Vegetable Curry"
                    isVeg -> "2 Ragi/Wheat Chapatis with Palak Paneer & Dal"
                    isEgg -> "2 Chapatis with 2-Egg Curry & Sauteed Vegetables"
                    else -> "2 Chapatis with 130g Pan-Seared Fish/Chicken & Stir-Fried Veggies"
                },
                portionSize = "1 light dinner plate (~380g)",
                calories = 490,
                proteinGrams = 30,
                carbsGrams = 52,
                fatGrams = 15,
                preparationInstructions = "Keep dinner lighter on oil and finish 2–3 hours before sleep for optimal recovery.",
                alternatives = listOf("Moong Dal Khichdi with Curd", "Vegetable Dalia Upma with Paneer Cubes")
            )
        )
    }
}
