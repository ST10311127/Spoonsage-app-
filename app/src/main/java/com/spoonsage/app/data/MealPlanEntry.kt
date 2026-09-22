package com.spoonsage.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One recipe placed on one day of the weekly plan (Mealime / BigOven style
 * weekly planning, Section 6).
 */
@Entity(tableName = "meal_plan_entries")
data class MealPlanEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Int,
    val recipeTitle: String,
    val recipeImageUrl: String?,
    /** Monday, Tuesday, ... Sunday */
    val dayOfWeek: String
)
