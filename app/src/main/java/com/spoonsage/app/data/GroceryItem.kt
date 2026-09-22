package com.spoonsage.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One line on the shopping list. Duplicate ingredient names added from
 * different recipes are merged in the repository (Mealime-style automatic
 * grocery list, Section 6, point 2).
 */
@Entity(tableName = "grocery_items")
data class GroceryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isChecked: Boolean = false
)
