package com.spoonsage.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Recipe::class, MealPlanEntry::class, GroceryItem::class,
        ProgressLog::class, Reminder::class, AiCoachMessage::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun groceryDao(): GroceryDao
    abstract fun progressDao(): ProgressDao
    abstract fun reminderDao(): ReminderDao
    abstract fun aiCoachDao(): AiCoachDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spoonsage.db"
                )
                    // Simple student-project approach: wipe and rebuild on schema
                    // changes instead of writing a formal Migration.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
