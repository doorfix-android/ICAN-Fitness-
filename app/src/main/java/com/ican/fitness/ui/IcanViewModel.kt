package com.ican.fitness.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ican.fitness.data.local.*
import com.ican.fitness.data.repository.IcanRepository
import com.ican.fitness.domain.GeneratedWorkoutPlan
import com.ican.fitness.domain.WorkoutGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class IcanViewModel(private val repository: IcanRepository) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allGoals: StateFlow<List<GoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedWorkouts: StateFlow<List<WorkoutSessionEntity>> = repository.completedWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val personalRecords: StateFlow<List<PersonalRecordEntity>> = repository.personalRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val measurements: StateFlow<List<BodyMeasurementEntity>> = repository.measurements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentMealPlans: StateFlow<List<MealPlanEntity>> = repository.recentMealPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val todayStr = LocalDate.now().toString()
    val todayMealPlan: StateFlow<MealPlanEntity?> = repository.getMealPlanForDate(todayStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _excludedExerciseIds = MutableStateFlow<Set<String>>(emptySet())
    private val _interruptedSession = MutableStateFlow<WorkoutSessionEntity?>(null)
    val interruptedSession: StateFlow<WorkoutSessionEntity?> = _interruptedSession.asStateFlow()

    private val _isResearchingGoal = MutableStateFlow(false)
    val isResearchingGoal: StateFlow<Boolean> = _isResearchingGoal.asStateFlow()

    private val _researchError = MutableStateFlow<String?>(null)
    val researchError: StateFlow<String?> = _researchError.asStateFlow()

    private val _isGeneratingDiet = MutableStateFlow(false)
    val isGeneratingDiet: StateFlow<Boolean> = _isGeneratingDiet.asStateFlow()

    val todayWorkoutPlan: StateFlow<GeneratedWorkoutPlan?> = combine(
        userProfile,
        completedWorkouts,
        _excludedExerciseIds
    ) { profile, sessions, excluded ->
        profile?.let {
            WorkoutGenerator.generateDailyPlan(
                date = LocalDate.now(),
                profile = it,
                recentSessions = sessions,
                excludeExerciseIds = excluded
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            _interruptedSession.value = repository.getInterruptedSession()
        }
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    fun refreshTodayWorkout() {
        val currentIds = todayWorkoutPlan.value?.exercises?.map { it.id }?.toSet() ?: emptySet()
        _excludedExerciseIds.value = if (_excludedExerciseIds.value.isEmpty()) currentIds else emptySet()
    }

    fun addWater(ml: Int) {
        viewModelScope.launch {
            repository.addWater(ml)
        }
    }

    fun researchGoal(query: String) {
        viewModelScope.launch {
            _isResearchingGoal.value = true
            _researchError.value = null
            val result = repository.researchAndSaveGoal(query)
            if (result.isFailure) {
                _researchError.value = "Could not reach research server. Check internet connection and try again."
            }
            _isResearchingGoal.value = false
        }
    }

    fun updateGoalStatus(goal: GoalEntity, status: String) {
        viewModelScope.launch {
            repository.updateGoal(goal.copy(status = status))
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun generateMealPlan(dateStr: String) {
        viewModelScope.launch {
            _isGeneratingDiet.value = true
            val isWorkout = !(todayWorkoutPlan.value?.isRestDay ?: false)
            repository.generateOrRefreshMealPlan(dateStr = dateStr, isWorkoutDay = isWorkout)
            _isGeneratingDiet.value = false
        }
    }

    fun toggleMealCompleted(plan: MealPlanEntity, mealType: String) {
        viewModelScope.launch {
            repository.toggleMealCompleted(plan, mealType)
        }
    }

    fun replaceMeal(plan: MealPlanEntity, mealType: String, newFoodName: String) {
        viewModelScope.launch {
            repository.replaceMealInPlan(plan, mealType, newFoodName)
        }
    }

    fun finishWorkout(session: WorkoutSessionEntity, records: List<Triple<String, Float, Int>>) {
        viewModelScope.launch {
            repository.saveWorkoutSession(session)
            _interruptedSession.value?.let { repository.deleteSessionById(it.id) }
            _interruptedSession.value = null
            records.forEach { (name, weight, reps) ->
                repository.checkAndSavePersonalRecord(name, weight, reps, session.dateStr)
            }
        }
    }

    fun saveInterruptedSession(session: WorkoutSessionEntity) {
        viewModelScope.launch {
            repository.saveWorkoutSession(session)
            _interruptedSession.value = repository.getInterruptedSession()
        }
    }

    fun saveMeasurement(measurement: BodyMeasurementEntity) {
        viewModelScope.launch {
            repository.saveMeasurement(measurement)
        }
    }

    fun deleteMeasurement(measurement: BodyMeasurementEntity) {
        viewModelScope.launch {
            repository.deleteMeasurement(measurement)
        }
    }

    suspend fun exportDataJson(): String = repository.exportAllUserDataJson()

    fun wipeAllData() {
        viewModelScope.launch {
            repository.wipeAllUserData()
        }
    }

    class Factory(private val repository: IcanRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return IcanViewModel(repository) as T
        }
    }
}
