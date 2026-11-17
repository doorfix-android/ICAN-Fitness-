package com.ican.fitness.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM fitness_goals ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)
}

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM daily_meal_plans WHERE dateStr = :dateStr LIMIT 1")
    fun getMealPlanForDate(dateStr: String): Flow<MealPlanEntity?>

    @Query("SELECT * FROM daily_meal_plans ORDER BY dateStr DESC LIMIT 14")
    fun getRecentMealPlans(): Flow<List<MealPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMealPlan(plan: MealPlanEntity)
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 1 ORDER BY timestamp DESC")
    fun getCompletedWorkouts(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 0 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getInterruptedSession(): WorkoutSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: WorkoutSessionEntity): Long

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("SELECT * FROM personal_records ORDER BY exerciseName ASC")
    fun getAllPersonalRecords(): Flow<List<PersonalRecordEntity>>

    @Query("SELECT * FROM personal_records WHERE exerciseName = :name LIMIT 1")
    suspend fun getRecordForExercise(name: String): PersonalRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecord(record: PersonalRecordEntity)
}

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM body_measurements ORDER BY timestamp DESC")
    fun getAllMeasurements(): Flow<List<BodyMeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeasurement(measurement: BodyMeasurementEntity)

    @Delete
    suspend fun deleteMeasurement(measurement: BodyMeasurementEntity)
}
