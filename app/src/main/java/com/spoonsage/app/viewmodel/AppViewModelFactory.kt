package com.spoonsage.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.spoonsage.app.data.FirestoreUserRepository
import com.spoonsage.app.data.RecipeRepository
import com.spoonsage.app.data.UserPreferences

/**
 * Simple manual factory (no Hilt) so every screen's ViewModel can be given
 * the shared repository, preferences store, and Firebase instances.
 */
class AppViewModelFactory(
    private val repository: RecipeRepository,
    private val preferences: UserPreferences,
    private val auth: FirebaseAuth,
    private val firestoreUserRepository: FirestoreUserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            AuthViewModel::class.java -> AuthViewModel(auth, firestoreUserRepository, preferences) as T
            ProfileSetupViewModel::class.java -> ProfileSetupViewModel(preferences, firestoreUserRepository) as T
            HomeDashboardViewModel::class.java -> HomeDashboardViewModel(repository, preferences) as T
            HomeViewModel::class.java -> HomeViewModel(repository, preferences) as T
            RecipeDetailViewModel::class.java -> RecipeDetailViewModel(repository) as T
            MealPlanViewModel::class.java -> MealPlanViewModel(repository) as T
            GroceryViewModel::class.java -> GroceryViewModel(repository) as T
            AiCoachViewModel::class.java -> AiCoachViewModel(repository, preferences) as T
            ProgressViewModel::class.java -> ProgressViewModel(repository, preferences) as T
            ProfileViewModel::class.java -> ProfileViewModel(repository, preferences, firestoreUserRepository, auth) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}
