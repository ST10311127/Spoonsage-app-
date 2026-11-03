package com.spoonsage.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Recipe::class,
        MealPlanEntry::class,
        GroceryItem::class,
        ProgressLog::class,
        Reminder::class,
        AiCoachMessage::class,
        LocalUser::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun groceryDao(): GroceryDao
    abstract fun progressDao(): ProgressDao
    abstract fun reminderDao(): ReminderDao
    abstract fun aiCoachDao(): AiCoachDao
    abstract fun localUserDao(): LocalUserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spoonsage.db"
                )

                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
