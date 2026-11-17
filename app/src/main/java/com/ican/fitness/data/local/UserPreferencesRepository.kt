package com.ican.fitness.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ican_user_prefs")

data class UserProfile(
    val onboardingCompleted: Boolean = false,
    val name: String = "",
    val age: Int = 25,
    val heightCm: Float = 170f,
    val weightKg: Float = 70f,
    val fitnessExperience: String = "Beginner",
    val primaryGoal: String = "Build Strength & Lean Muscle",
    val secondaryGoal: String = "Improve Stamina",
    val targetMuscles: String = "Full Body, Chest, Back, Core",
    val availableEquipment: String = "Bodyweight, Dumbbells",
    val workoutDurationMinutes: Int = 45,
    val weeklyWorkoutDays: String = "MON,WED,FRI,SAT",
    val dietaryPreference: String = "Vegetarian",
    val foodAllergies: String = "None",
    val activityLevel: String = "Moderately Active",
    val alarmHour: Int = 6,
    val alarmMinute: Int = 30,
    val alarmEnabled: Boolean = false,
    val beginnerMode: Boolean = true,
    val hydrationReminders: Boolean = true,
    val mealReminders: Boolean = true,
    val dailyWaterMl: Int = 0,
    val waterDate: String = ""
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val NAME = stringPreferencesKey("name")
        val AGE = intPreferencesKey("age")
        val HEIGHT_CM = floatPreferencesKey("height_cm")
        val WEIGHT_KG = floatPreferencesKey("weight_kg")
        val EXPERIENCE = stringPreferencesKey("experience")
        val PRIMARY_GOAL = stringPreferencesKey("primary_goal")
        val SECONDARY_GOAL = stringPreferencesKey("secondary_goal")
        val TARGET_MUSCLES = stringPreferencesKey("target_muscles")
        val EQUIPMENT = stringPreferencesKey("equipment")
        val DURATION_MIN = intPreferencesKey("duration_min")
        val WEEKLY_DAYS = stringPreferencesKey("weekly_days")
        val DIET_PREF = stringPreferencesKey("diet_pref")
        val ALLERGIES = stringPreferencesKey("allergies")
        val ACTIVITY_LEVEL = stringPreferencesKey("activity_level")
        val ALARM_HOUR = intPreferencesKey("alarm_hour")
        val ALARM_MINUTE = intPreferencesKey("alarm_minute")
        val ALARM_ENABLED = booleanPreferencesKey("alarm_enabled")
        val BEGINNER_MODE = booleanPreferencesKey("beginner_mode")
        val HYDRATION_REMINDERS = booleanPreferencesKey("hydration_reminders")
        val MEAL_REMINDERS = booleanPreferencesKey("meal_reminders")
        val DAILY_WATER_ML = intPreferencesKey("daily_water_ml")
        val WATER_DATE = stringPreferencesKey("water_date")
    }

    val userProfileFlow: Flow<UserProfile> = context.dataStore.data.map { prefs ->
        UserProfile(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            name = prefs[Keys.NAME] ?: "",
            age = prefs[Keys.AGE] ?: 25,
            heightCm = prefs[Keys.HEIGHT_CM] ?: 170f,
            weightKg = prefs[Keys.WEIGHT_KG] ?: 70f,
            fitnessExperience = prefs[Keys.EXPERIENCE] ?: "Beginner",
            primaryGoal = prefs[Keys.PRIMARY_GOAL] ?: "Build Strength & Lean Muscle",
            secondaryGoal = prefs[Keys.SECONDARY_GOAL] ?: "Improve Stamina",
            targetMuscles = prefs[Keys.TARGET_MUSCLES] ?: "Full Body, Chest, Back, Core",
            availableEquipment = prefs[Keys.EQUIPMENT] ?: "Bodyweight, Dumbbells",
            workoutDurationMinutes = prefs[Keys.DURATION_MIN] ?: 45,
            weeklyWorkoutDays = prefs[Keys.WEEKLY_DAYS] ?: "MON,WED,FRI,SAT",
            dietaryPreference = prefs[Keys.DIET_PREF] ?: "Vegetarian",
            foodAllergies = prefs[Keys.ALLERGIES] ?: "None",
            activityLevel = prefs[Keys.ACTIVITY_LEVEL] ?: "Moderately Active",
            alarmHour = prefs[Keys.ALARM_HOUR] ?: 6,
            alarmMinute = prefs[Keys.ALARM_MINUTE] ?: 30,
            alarmEnabled = prefs[Keys.ALARM_ENABLED] ?: false,
            beginnerMode = prefs[Keys.BEGINNER_MODE] ?: true,
            hydrationReminders = prefs[Keys.HYDRATION_REMINDERS] ?: true,
            mealReminders = prefs[Keys.MEAL_REMINDERS] ?: true,
            dailyWaterMl = prefs[Keys.DAILY_WATER_ML] ?: 0,
            waterDate = prefs[Keys.WATER_DATE] ?: ""
        )
    }

    suspend fun saveProfile(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = profile.onboardingCompleted
            prefs[Keys.NAME] = profile.name
            prefs[Keys.AGE] = profile.age
            prefs[Keys.HEIGHT_CM] = profile.heightCm
            prefs[Keys.WEIGHT_KG] = profile.weightKg
            prefs[Keys.EXPERIENCE] = profile.fitnessExperience
            prefs[Keys.PRIMARY_GOAL] = profile.primaryGoal
            prefs[Keys.SECONDARY_GOAL] = profile.secondaryGoal
            prefs[Keys.TARGET_MUSCLES] = profile.targetMuscles
            prefs[Keys.EQUIPMENT] = profile.availableEquipment
            prefs[Keys.DURATION_MIN] = profile.workoutDurationMinutes
            prefs[Keys.WEEKLY_DAYS] = profile.weeklyWorkoutDays
            prefs[Keys.DIET_PREF] = profile.dietaryPreference
            prefs[Keys.ALLERGIES] = profile.foodAllergies
            prefs[Keys.ACTIVITY_LEVEL] = profile.activityLevel
            prefs[Keys.ALARM_HOUR] = profile.alarmHour
            prefs[Keys.ALARM_MINUTE] = profile.alarmMinute
            prefs[Keys.ALARM_ENABLED] = profile.alarmEnabled
            prefs[Keys.BEGINNER_MODE] = profile.beginnerMode
            prefs[Keys.HYDRATION_REMINDERS] = profile.hydrationReminders
            prefs[Keys.MEAL_REMINDERS] = profile.mealReminders
        }
    }

    suspend fun updateWaterIntake(dateStr: String, ml: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WATER_DATE] = dateStr
            prefs[Keys.DAILY_WATER_ML] = ml.coerceAtLeast(0)
        }
    }

    suspend fun clearAllPreferences() {
        context.dataStore.edit { it.clear() }
    }
}
