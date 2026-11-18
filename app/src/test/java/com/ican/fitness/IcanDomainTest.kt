package com.ican.fitness

import com.ican.fitness.alarm.AlarmScheduler
import com.ican.fitness.data.local.UserProfile
import com.ican.fitness.domain.ExerciseLibrary
import com.ican.fitness.domain.WorkoutGenerator
import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class IcanDomainTest {

    @Test
    fun `exercise library covers all 12 required categories`() {
        val categories = ExerciseLibrary.allExercises.map { it.category }.distinct()
        assertEquals(12, categories.size)
    }

    @Test
    fun `workout generator honors rest days and generates active recovery`() {
        val profile = UserProfile(weeklyWorkoutDays = "MON,WED,FRI")
        val sunday = LocalDate.of(2026, 11, 22)
        assertEquals(DayOfWeek.SUNDAY, sunday.dayOfWeek)

        val plan = WorkoutGenerator.generateDailyPlan(
            date = sunday,
            profile = profile,
            recentSessions = emptyList()
        )
        assertTrue(plan.isRestDay)
        assertTrue(plan.exercises.isNotEmpty())
    }

    @Test
    fun `workout generator builds strength session on scheduled training day`() {
        val profile = UserProfile(
            weeklyWorkoutDays = "MON,WED,FRI",
            availableEquipment = "Bodyweight, Dumbbells",
            workoutDurationMinutes = 45
        )
        val monday = LocalDate.of(2026, 11, 23)
        val plan = WorkoutGenerator.generateDailyPlan(
            date = monday,
            profile = profile,
            recentSessions = emptyList()
        )
        assertFalse(plan.isRestDay)
        assertEquals(5, plan.exercises.size)
    }

    @Test
    fun `alarm scheduler computes future trigger timestamp`() {
        val triggerMillis = AlarmScheduler.computeNextAlarmTriggerMillis(
            hour = 6,
            minute = 30,
            weeklyDaysCsv = "MON,TUE,WED,THU,FRI,SAT,SUN"
        )
        assertNotNull(triggerMillis)
        assertTrue(triggerMillis!! > System.currentTimeMillis())
    }
}
