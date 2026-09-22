package com.spoonsage.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spoonsage.app.data.ComplexSearchResult
import com.spoonsage.app.data.RecipeRepository
import com.spoonsage.app.data.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class SearchMode { BY_NAME, BY_INGREDIENTS }

/**
 * Home / discovery screen: search by name (like BigOven's big catalogue)
 * or by ingredients you already have (BigOven's "Use Up Leftovers" /
 * SideChef's "Search by Ingredients", Section 6, point 4).
 */
class HomeViewModel(
    private val repository: RecipeRepository,
    private val preferences: UserPreferences
) : ViewModel() {

    var searchMode by mutableStateOf(SearchMode.BY_NAME)
        private set

    var queryText by mutableStateOf("")
        private set

    var results by mutableStateOf<List<ComplexSearchResult>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun setSearchMode(mode: SearchMode) {
        searchMode = mode
        results = emptyList()
        errorMessage = null
    }

    fun onQueryChange(text: String) {
        queryText = text
    }

    fun search() {
        if (queryText.isBlank()) return
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val diet = preferences.diet.first()
            val result = if (searchMode == SearchMode.BY_INGREDIENTS) {
                val ingredientList = queryText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                repository.searchByIngredients(ingredientList)
            } else {
                repository.searchByQuery(queryText, diet, null)
            }
            result
                .onSuccess { results = it }
                .onFailure { errorMessage = it.message ?: "Something went wrong. Check your internet connection." }
            isLoading = false
        }
    }
}
