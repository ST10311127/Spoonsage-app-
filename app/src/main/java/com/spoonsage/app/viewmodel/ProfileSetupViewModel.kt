package com.spoonsage.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spoonsage.app.data.FirestoreUserRepository
import com.spoonsage.app.data.Goals
import com.spoonsage.app.data.PersonalInfo
import com.spoonsage.app.data.UserPreferences
import kotlinx.coroutines.launch

val DIET_OPTIONS = listOf(
    "Classic", "Vegetarian", "Vegan", "Pescatarian", "Halal", "Kosher", "Paleo", "Keto", "Low Carb"
)

val DISLIKE_OPTIONS = listOf(
    "Avocado", "Beef", "Beets", "Bell peppers", "Blue cheese", "Brussels sprouts",
    "Cauliflower", "Eggplant", "Eggs", "Goat cheese", "Lamb", "Mushrooms",
    "Olives", "Pork", "Quinoa", "Shrimp", "Tofu", "Turnips"
)

val ACTIVITY_LEVELS = listOf("Low", "Medium", "High")
val GENDER_OPTIONS = listOf("Female", "Male", "Non-binary", "Prefer not to say")
val GOAL_TYPES = listOf("Lose weight", "Eat healthier", "Drink more water", "Build muscle")

/**
 * Drives the post-sign-up profile wizard: personal info, health info,
 * dietary preferences and goals - one question group per screen, mirroring
 * the Mealime-style sequential onboarding and the design doc's
 * "Sign Up / Profile Setup Wizard" step (Figure 2, Section 3.1.2).
 */
class ProfileSetupViewModel(
    private val preferences: UserPreferences,
    private val firestoreUserRepository: FirestoreUserRepository
) : ViewModel() {

    // Personal info
    var displayName by mutableStateOf("")
        private set
    var age by mutableStateOf("")
        private set
    var gender by mutableStateOf(GENDER_OPTIONS.last())
        private set

    // Health info
    var heightCm by mutableStateOf("")
        private set
    var weightKg by mutableStateOf("")
        private set
    var activityLevel by mutableStateOf("Medium")
        private set

    // Dietary preferences
    var selectedDiet by mutableStateOf("Classic")
        private set
    var selectedDislikes by mutableStateOf(setOf<String>())
        private set

    // Goals
    var goalType by mutableStateOf(GOAL_TYPES[1])
        private set
    var targetWeightKg by mutableStateOf("")
        private set
    var dailyCalorieTarget by mutableStateOf("2200")
        private set
    var dailyProteinTargetG by mutableStateOf("100")
        private set
    var dailyWaterTargetL by mutableStateOf("2.5")
        private set

    fun onDisplayNameChange(v: String) { displayName = v }
    fun onAgeChange(v: String) { age = v.filter { it.isDigit() } }
    fun onGenderChange(v: String) { gender = v }
    fun onHeightChange(v: String) { heightCm = v.filter { it.isDigit() || it == '.' } }
    fun onWeightChange(v: String) { weightKg = v.filter { it.isDigit() || it == '.' } }
    fun onActivityChange(v: String) { activityLevel = v }
    fun selectDiet(diet: String) { selectedDiet = diet }
    fun toggleDislike(item: String) {
        selectedDislikes = if (selectedDislikes.contains(item)) selectedDislikes - item else selectedDislikes + item
    }
    fun onGoalTypeChange(v: String) { goalType = v }
    fun onTargetWeightChange(v: String) { targetWeightKg = v.filter { it.isDigit() || it == '.' } }
    fun onCalorieTargetChange(v: String) { dailyCalorieTarget = v.filter { it.isDigit() } }
    fun onProteinTargetChange(v: String) { dailyProteinTargetG = v.filter { it.isDigit() } }
    fun onWaterTargetChange(v: String) { dailyWaterTargetL = v.filter { it.isDigit() || it == '.' } }

    fun finish(onDone: () -> Unit) {
        viewModelScope.launch {
            val personalInfo = PersonalInfo(
                displayName = displayName.ifBlank { "there" },
                age = age.toIntOrNull() ?: 0,
                gender = gender,
                heightCm = heightCm.toFloatOrNull() ?: 0f,
                weightKg = weightKg.toFloatOrNull() ?: 0f,
                activityLevel = activityLevel
            )
            val goals = Goals(
                goalType = goalType,
                targetWeightKg = targetWeightKg.toFloatOrNull() ?: 0f,
                dailyCalorieTarget = dailyCalorieTarget.toIntOrNull() ?: 2200,
                dailyProteinTargetG = dailyProteinTargetG.toIntOrNull() ?: 100,
                dailyWaterTargetL = dailyWaterTargetL.toFloatOrNull() ?: 2.5f
            )
            val dislikes = selectedDislikes.toList()

            preferences.savePersonalInfo(personalInfo)
            preferences.saveDietaryPreferences(selectedDiet, dislikes)
            preferences.saveGoals(goals)
            preferences.markProfileSetupDone()

            // Cloud copy in Firestore, keyed by the Firebase Auth uid (guests get an anonymous uid too).
            firestoreUserRepository.syncProfile(personalInfo, goals, selectedDiet, dislikes)

            onDone()
        }
    }
}
