package com.spoonsage.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.spoonsage.app.data.FirestoreUserRepository
import com.spoonsage.app.data.Goals
import com.spoonsage.app.data.PersonalInfo
import com.spoonsage.app.data.RecipeRepository
import com.spoonsage.app.data.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Profile tab (Section 3.2.9): account details, personal/health info, dietary prefs, goals, reminders. */
class ProfileViewModel(
    private val repository: RecipeRepository,
    private val preferences: UserPreferences,
    private val firestoreUserRepository: FirestoreUserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val email = preferences.email
    val personalInfo = preferences.personalInfo
    val goals = preferences.goals
    val diet = preferences.diet
    val dislikes = preferences.dislikes
    val reminders = repository.getReminders()
    val isGuest = preferences.isGuest

    fun savePersonalInfo(info: PersonalInfo) {
        viewModelScope.launch {
            preferences.savePersonalInfo(info)
            syncToFirestore()
        }
    }

    fun saveGoals(goals: Goals) {
        viewModelScope.launch {
            preferences.saveGoals(goals)
            syncToFirestore()
        }
    }

    fun saveDietaryPreferences(diet: String, dislikes: List<String>) {
        viewModelScope.launch {
            preferences.saveDietaryPreferences(diet, dislikes)
            syncToFirestore()
        }
    }

    /** Pushes the full current profile (personal info + goals + diet) up to Firestore. */
    private suspend fun syncToFirestore() {
        firestoreUserRepository.syncProfile(
            personalInfo = preferences.personalInfo.first(),
            goals = preferences.goals.first(),
            diet = preferences.diet.first(),
            dislikes = preferences.dislikes.first().split(",").map { it.trim() }.filter { it.isNotEmpty() }
        )
    }

    fun addReminder(type: String, time: String) {
        viewModelScope.launch { repository.addReminder(type, time) }
    }

    fun toggleReminder(reminder: com.spoonsage.app.data.Reminder) {
        viewModelScope.launch { repository.toggleReminder(reminder) }
    }

    fun deleteReminder(reminder: com.spoonsage.app.data.Reminder) {
        viewModelScope.launch { repository.deleteReminder(reminder) }
    }

    fun logOut(onDone: () -> Unit) {
        viewModelScope.launch {
            auth.signOut()
            preferences.logOut()
            onDone()
        }
    }
}
