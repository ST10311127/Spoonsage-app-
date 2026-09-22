package com.spoonsage.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spoonsage.app.data.GroceryItem
import com.spoonsage.app.data.RecipeRepository
import kotlinx.coroutines.launch

/** Auto-combined grocery list, checkable while shopping (Section 6, point 2). */
class GroceryViewModel(private val repository: RecipeRepository) : ViewModel() {

    val groceryItems = repository.getGroceryList()

    fun toggleChecked(item: GroceryItem) {
        viewModelScope.launch { repository.setGroceryChecked(item, !item.isChecked) }
    }

    fun delete(item: GroceryItem) {
        viewModelScope.launch { repository.deleteGroceryItem(item) }
    }

    fun addManualItem(name: String) {
        viewModelScope.launch { repository.addGroceryItem(name) }
    }

    fun clearChecked() {
        viewModelScope.launch { repository.clearCheckedGroceryItems() }
    }
}
