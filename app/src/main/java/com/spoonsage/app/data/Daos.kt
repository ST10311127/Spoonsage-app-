package com.spoonsage.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(recipe: Recipe)

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getById(id: Int): Recipe?

    @Query("SELECT * FROM recipes WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavorites(): Flow<List<Recipe>>

    @Update
    suspend fun update(recipe: Recipe)
}

@Dao
interface MealPlanDao {
    @Insert
    suspend fun insert(entry: MealPlanEntry)

    @Delete
    suspend fun delete(entry: MealPlanEntry)

    @Query("SELECT * FROM meal_plan_entries ORDER BY dayOfWeek ASC")
    fun getWeekPlan(): Flow<List<MealPlanEntry>>

    @Query("DELETE FROM meal_plan_entries")
    suspend fun clearAll()
}

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: ProgressLog)

    @Query("SELECT * FROM progress_logs WHERE date = :date")
    suspend fun getByDate(date: String): ProgressLog?

    @Query("SELECT * FROM progress_logs WHERE date = :date")
    fun observeByDate(date: String): Flow<ProgressLog?>

    @Query("SELECT * FROM progress_logs ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int = 7): Flow<List<ProgressLog>>
}

@Dao
interface ReminderDao {
    @Insert
    suspend fun insert(reminder: Reminder)

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("SELECT * FROM reminders ORDER BY time ASC")
    fun getAll(): Flow<List<Reminder>>
}

@Dao
interface AiCoachDao {
    @Insert
    suspend fun insert(message: AiCoachMessage)

    @Query("SELECT * FROM ai_coach_messages ORDER BY timestamp ASC")
    fun getAll(): Flow<List<AiCoachMessage>>

    @Query("DELETE FROM ai_coach_messages")
    suspend fun clearAll()
}

@Dao
interface GroceryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: GroceryItem)

    @Update
    suspend fun update(item: GroceryItem)

    @Delete
    suspend fun delete(item: GroceryItem)

    @Query("SELECT * FROM grocery_items WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): GroceryItem?

    @Query("SELECT * FROM grocery_items ORDER BY isChecked ASC, name ASC")
    fun getAll(): Flow<List<GroceryItem>>

    @Query("DELETE FROM grocery_items WHERE isChecked = 1")
    suspend fun clearChecked()
}
