package com.spoonsage.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spoonsage.app.data.MealPlanEntry
import com.spoonsage.app.data.RecipeRepository
import kotlinx.coroutines.launch

/** Weekly meal plan screen (Section 6, point 1 / 8, BigOven-style calendar). */
class MealPlanViewModel(private val repository: RecipeRepository) : ViewModel() {

    val weekPlan = repository.getWeekPlan()

    fun remove(entry: MealPlanEntry) {
        viewModelScope.launch { repository.removeFromMealPlan(entry) }
    }

    fun clearWeek() {
        viewModelScope.launch { repository.clearMealPlan() }
    }
}
