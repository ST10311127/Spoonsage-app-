package com.spoonsage.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: Int,
    val title: String,
    val imageUrl: String?,
    val readyInMinutes: Int,
    val servings: Int,

    val ingredientsRaw: String,

    val stepsRaw: String,
    val isFavorite: Boolean = false,
    val calories: Int,
    val protein: Int = 0,
    val difficulty: String
)

val Recipe.ingredients: List<String> get() = ingredientsRaw.lines().filter { it.isNotBlank() }
val Recipe.steps: List<String> get() = stepsRaw.lines().filter { it.isNotBlank() }

@Entity(tableName = "meal_plan_entries")
data class MealPlanEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val recipeId: Int,
    val recipeTitle: String,
    val recipeImageUrl: String?,
    val dayOfWeek: String,
    val mealType: String,
    val calories: Int,
    val synced: Boolean = false,
    val pendingDelete: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "grocery_items")
data class GroceryItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isChecked: Boolean = false,
    val synced: Boolean = false,
    val pendingDelete: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "progress_logs")
data class ProgressLog(
    @PrimaryKey val date: String,
    val caloriesConsumed: Int,
    val proteinConsumedG: Int,
    val waterConsumedL: Double,
    val streakCount: Int,
    val synced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,

    val time: String,
    val isEnabled: Boolean = true
)

@Entity(tableName = "ai_coach_messages")
data class AiCoachMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedRecipeTitle: String? = null
) {
    companion object {
        const val SENDER_USER = "user"
        const val SENDER_COACH = "coach"
    }
}

val AiCoachMessage.isFromUser: Boolean get() = sender == AiCoachMessage.SENDER_USER

@Entity(tableName = "local_users")
data class LocalUser(
    @PrimaryKey val email: String,
    val uid: String,
    val displayName: String,
    val passwordHash: String,
    val salt: String
)
