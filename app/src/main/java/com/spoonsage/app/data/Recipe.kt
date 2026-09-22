package com.spoonsage.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A recipe cached locally so it can still be viewed offline once it has
 * been loaded once (Section 6, point 6 - offline-first storage).
 */
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: Int,
    val title: String,
    val imageUrl: String?,
    val readyInMinutes: Int,
    val servings: Int,
    /** Ingredients joined into one string, e.g. "2 eggs|1 cup flour|..." */
    val ingredientsRaw: String,
    /** Steps joined the same way, one instruction per entry. */
    val stepsRaw: String,
    val isFavorite: Boolean = false,
    val calories: Int = 0,
    /** "Easy" | "Medium" | "Hard" - derived from readyInMinutes. */
    val difficulty: String = "Easy"
) {
    val ingredients: List<String>
        get() = if (ingredientsRaw.isBlank()) emptyList() else ingredientsRaw.split("|")

    val steps: List<String>
        get() = if (stepsRaw.isBlank()) emptyList() else stepsRaw.split("|")
}
