package com.spoonsage.app.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(recipe: Recipe)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(recipes: List<Recipe>)

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getById(id: Int): Recipe?

    @Query("SELECT * FROM recipes WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Int>): List<Recipe>

    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :query || '%' ORDER BY title ASC")
    suspend fun searchCached(query: String): List<Recipe>

    @Query("SELECT * FROM recipes WHERE ingredientsRaw LIKE '%' || :ingredient || '%' ORDER BY title ASC")
    suspend fun searchCachedByIngredient(ingredient: String): List<Recipe>

    @Query("SELECT * FROM recipes WHERE isFavorite = 1 ORDER BY title ASC")
    fun observeFavorites(): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipes ORDER BY title ASC LIMIT :limit")
    suspend fun getSome(limit: Int): List<Recipe>

    @Query("UPDATE recipes SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Int, favorite: Boolean)

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun count(): Int
}

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plan_entries WHERE pendingDelete = 0")
    fun observeAll(): LiveData<List<MealPlanEntry>>

    @Query("SELECT * FROM meal_plan_entries WHERE pendingDelete = 0 AND dayOfWeek = :day")
    fun observeForDay(day: String): LiveData<List<MealPlanEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: MealPlanEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<MealPlanEntry>)

    @Query("SELECT * FROM meal_plan_entries WHERE synced = 0")
    suspend fun getUnsynced(): List<MealPlanEntry>

    @Query("SELECT * FROM meal_plan_entries WHERE pendingDelete = 0")
    suspend fun getAll(): List<MealPlanEntry>

    @Query("UPDATE meal_plan_entries SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("UPDATE meal_plan_entries SET pendingDelete = 1, synced = 0 WHERE id = :id")
    suspend fun markDeleted(id: String)

    @Query("UPDATE meal_plan_entries SET pendingDelete = 1, synced = 0")
    suspend fun markAllDeleted()

    @Query("DELETE FROM meal_plan_entries WHERE id = :id")
    suspend fun hardDelete(id: String)

    @Query("DELETE FROM meal_plan_entries")
    suspend fun clear()
}

@Dao
interface GroceryDao {
    @Query("SELECT * FROM grocery_items WHERE pendingDelete = 0 ORDER BY isChecked ASC, name COLLATE NOCASE ASC")
    fun observeAll(): LiveData<List<GroceryItem>>

    @Query("SELECT * FROM grocery_items WHERE pendingDelete = 0 AND name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): GroceryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: GroceryItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<GroceryItem>)

    @Query("SELECT * FROM grocery_items WHERE synced = 0")
    suspend fun getUnsynced(): List<GroceryItem>

    @Query("SELECT * FROM grocery_items WHERE pendingDelete = 0 AND isChecked = 1")
    suspend fun getChecked(): List<GroceryItem>

    @Query("UPDATE grocery_items SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("UPDATE grocery_items SET pendingDelete = 1, synced = 0 WHERE id = :id")
    suspend fun markDeleted(id: String)

    @Query("DELETE FROM grocery_items WHERE id = :id")
    suspend fun hardDelete(id: String)

    @Query("DELETE FROM grocery_items")
    suspend fun clear()
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress_logs WHERE date = :date")
    suspend fun getByDate(date: String): ProgressLog?

    @Query("SELECT * FROM progress_logs WHERE date = :date")
    fun observeByDate(date: String): LiveData<ProgressLog?>

    @Query("SELECT * FROM progress_logs ORDER BY date DESC LIMIT :limit")
    fun observeRecent(limit: Int): LiveData<List<ProgressLog>>

    @Query("SELECT * FROM progress_logs WHERE date < :date ORDER BY date DESC LIMIT 1")
    suspend fun getLatestBefore(date: String): ProgressLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: ProgressLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(logs: List<ProgressLog>)

    @Query("SELECT * FROM progress_logs WHERE synced = 0")
    suspend fun getUnsynced(): List<ProgressLog>

    @Query("UPDATE progress_logs SET synced = 1 WHERE date = :date")
    suspend fun markSynced(date: String)

    @Query("DELETE FROM progress_logs")
    suspend fun clear()
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY time ASC")
    fun observeAll(): LiveData<List<Reminder>>

    @Query("SELECT * FROM reminders")
    suspend fun getAll(): List<Reminder>

    @Insert
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("DELETE FROM reminders")
    suspend fun clear()
}

@Dao
interface AiCoachDao {
    @Query("SELECT * FROM ai_coach_messages ORDER BY timestamp ASC")
    fun observeAll(): LiveData<List<AiCoachMessage>>

    @Query("SELECT * FROM ai_coach_messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<AiCoachMessage>

    @Insert
    suspend fun insert(message: AiCoachMessage): Long

    @Query("DELETE FROM ai_coach_messages")
    suspend fun clear()
}

@Dao
interface LocalUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: LocalUser)

    @Query("SELECT * FROM local_users WHERE email = :email COLLATE NOCASE LIMIT 1")
    suspend fun findByEmail(email: String): LocalUser?
}
