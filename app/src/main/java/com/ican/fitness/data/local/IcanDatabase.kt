package com.ican.fitness.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        GoalEntity::class,
        MealPlanEntity::class,
        WorkoutSessionEntity::class,
        BodyMeasurementEntity::class,
        PersonalRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class IcanDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun measurementDao(): MeasurementDao

    companion object {
        @Volatile
        private var INSTANCE: IcanDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Non-destructive migration scaffold for future schema updates
            }
        }

        fun getInstance(context: Context): IcanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IcanDatabase::class.java,
                    "ican_fitness_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
