package com.spoonsage.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spoonsage.app.data.ComplexSearchResult
import com.spoonsage.app.data.Goals
import com.spoonsage.app.data.RecipeRepository
import com.spoonsage.app.data.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Conversational AI Coach (Section 2, Section 5.1). Sends the user's
 * message plus a short context object to Gemini and can turn a suggested
 * dish into a real Spoonacular recipe added to the weekly plan.
 */
class AiCoachViewModel(
    private val repository: RecipeRepository,
    private val preferences: UserPreferences
) : ViewModel() {

    val messages = repository.getCoachMessages()

    var draftText by mutableStateOf("")
        private set
    var isSending by mutableStateOf(false)
        private set

    var pendingSuggestion by mutableStateOf<ComplexSearchResult?>(null)
        private set
    var suggestionError by mutableStateOf<String?>(null)
        private set

    fun onDraftChange(value: String) { draftText = value }

    fun send() {
        val text = draftText.trim()
        if (text.isEmpty() || isSending) return
        draftText = ""
        isSending = true
        viewModelScope.launch {
            val goals: Goals = preferences.goals.first()
            val diet = preferences.diet.first()
            val dislikes = preferences.dislikes.first()
            val context = buildString {
                append("Goal: ${goals.goalType}. ")
                append("Daily targets: ${goals.dailyCalorieTarget} kcal, ${goals.dailyProteinTargetG}g protein, ${goals.dailyWaterTargetL}L water. ")
                append("Diet: $diet. ")
                if (dislikes.isNotBlank()) append("Avoids: $dislikes.")
            }
            repository.sendCoachMessage(text, context)
            isSending = false
        }
    }

    fun sendQuickPrompt(text: String) {
        draftText = text
        send()
    }

    /** Looks up a real recipe for an AI Coach-suggested dish name so it can be shown as an addable card. */
    fun prepareSuggestion(title: String) {
        suggestionError = null
        viewModelScope.launch {
            repository.findRecipeForSuggestion(title)
                .onSuccess { pendingSuggestion = it }
                .onFailure { suggestionError = it.message ?: "Couldn't find a matching recipe." }
        }
    }

    fun dismissSuggestion() {
        pendingSuggestion = null
        suggestionError = null
    }
}
