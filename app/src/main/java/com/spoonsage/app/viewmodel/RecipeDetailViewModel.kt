package com.spoonsage.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spoonsage.app.data.Recipe
import com.spoonsage.app.data.RecipeRepository
import kotlinx.coroutines.launch

val DAYS_OF_WEEK = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

/**
 * Loads one recipe's full detail (ingredients + steps), lets the user
 * favorite it (BigOven-style favorites, Section 6, point 5) and add it to
 * a day of the weekly meal plan (Mealime-style planning, Section 6, point 1).
 */
class RecipeDetailViewModel(private val repository: RecipeRepository) : ViewModel() {

    var recipe by mutableStateOf<Recipe?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var addedToPlanMessage by mutableStateOf<String?>(null)
        private set

    fun load(recipeId: Int) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            repository.getRecipeDetail(recipeId)
                .onSuccess { recipe = it }
                .onFailure { errorMessage = it.message ?: "Could not load this recipe." }
            isLoading = false
        }
    }

    fun toggleFavorite() {
        val current = recipe ?: return
        viewModelScope.launch {
            repository.toggleFavorite(current)
            recipe = current.copy(isFavorite = !current.isFavorite)
        }
    }

    fun addToMealPlan(day: String) {
        val current = recipe ?: return
        viewModelScope.launch {
            repository.addToMealPlan(current, day)
            addedToPlanMessage = "Added to $day and its ingredients were added to your grocery list."
        }
    }

    fun clearAddedMessage() {
        addedToPlanMessage = null
    }
}
