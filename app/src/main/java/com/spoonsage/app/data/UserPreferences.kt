package com.spoonsage.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "spoonsage_prefs")

data class PersonalInfo(
    val displayName: String = "",
    val age: Int = 0,
    val gender: String = "Prefer not to say",
    val heightCm: Float = 0f,
    val weightKg: Float = 0f,
    val activityLevel: String = "Medium"
)

data class Goals(
    val goalType: String = "Eat healthier",
    val targetWeightKg: Float = 0f,
    val dailyCalorieTarget: Int = 2200,
    val dailyProteinTargetG: Int = 100,
    val dailyWaterTargetL: Float = 2.5f
)

/**
 * On-device cache for small, per-user values (Section 7 - User Account /
 * Personal Info / Goals entities): onboarding flags, personal/health info,
 * dietary preferences and goals. Real sign up/log in/guest accounts are
 * now handled by Firebase Authentication, and the cloud copy of this data
 * lives in Cloud Firestore (see FirestoreUserRepository) - this class just
 * keeps a local DataStore copy so the app still works offline.
 */
class UserPreferences(private val context: Context) {

    private object Keys {
        // Onboarding / profile-setup flags
        val PROFILE_SETUP_DONE = booleanPreferencesKey("profile_setup_done")

        // "Account" - local mirror of Firebase Auth state (real auth lives server-side)
        val HAS_ACCOUNT = booleanPreferencesKey("has_account")
        val IS_GUEST = booleanPreferencesKey("is_guest")
        val EMAIL = stringPreferencesKey("email")

        // Dietary preferences (reused from the original onboarding)
        val DIET = stringPreferencesKey("diet")
        val DISLIKES = stringPreferencesKey("dislikes") // comma separated

        // Personal + health info
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val AGE = intPreferencesKey("age")
        val GENDER = stringPreferencesKey("gender")
        val HEIGHT_CM = floatPreferencesKey("height_cm")
        val WEIGHT_KG = floatPreferencesKey("weight_kg")
        val ACTIVITY_LEVEL = stringPreferencesKey("activity_level")

        // Goals
        val GOAL_TYPE = stringPreferencesKey("goal_type")
        val TARGET_WEIGHT_KG = floatPreferencesKey("target_weight_kg")
        val DAILY_CALORIE_TARGET = intPreferencesKey("daily_calorie_target")
        val DAILY_PROTEIN_TARGET_G = intPreferencesKey("daily_protein_target_g")
        val DAILY_WATER_TARGET_L = floatPreferencesKey("daily_water_target_l")
    }

    // ---- Flows ----

    val profileSetupDone: Flow<Boolean> = context.dataStore.data.map { it[Keys.PROFILE_SETUP_DONE] ?: false }
    val hasAccount: Flow<Boolean> = context.dataStore.data.map { it[Keys.HAS_ACCOUNT] ?: false }
    val isGuest: Flow<Boolean> = context.dataStore.data.map { it[Keys.IS_GUEST] ?: false }
    val email: Flow<String> = context.dataStore.data.map { it[Keys.EMAIL] ?: "" }

    val diet: Flow<String> = context.dataStore.data.map { it[Keys.DIET] ?: "Classic" }
    val dislikes: Flow<String> = context.dataStore.data.map { it[Keys.DISLIKES] ?: "" }

    val personalInfo: Flow<PersonalInfo> = context.dataStore.data.map { prefs ->
        PersonalInfo(
            displayName = prefs[Keys.DISPLAY_NAME] ?: "",
            age = prefs[Keys.AGE] ?: 0,
            gender = prefs[Keys.GENDER] ?: "Prefer not to say",
            heightCm = prefs[Keys.HEIGHT_CM] ?: 0f,
            weightKg = prefs[Keys.WEIGHT_KG] ?: 0f,
            activityLevel = prefs[Keys.ACTIVITY_LEVEL] ?: "Medium"
        )
    }

    val goals: Flow<Goals> = context.dataStore.data.map { prefs ->
        Goals(
            goalType = prefs[Keys.GOAL_TYPE] ?: "Eat healthier",
            targetWeightKg = prefs[Keys.TARGET_WEIGHT_KG] ?: 0f,
            dailyCalorieTarget = prefs[Keys.DAILY_CALORIE_TARGET] ?: 2200,
            dailyProteinTargetG = prefs[Keys.DAILY_PROTEIN_TARGET_G] ?: 100,
            dailyWaterTargetL = prefs[Keys.DAILY_WATER_TARGET_L] ?: 2.5f
        )
    }

    // ---- Writers ----

    /** Legacy single-step diet onboarding, kept for the "By name" recipe search filter. */
    suspend fun saveDietaryPreferences(diet: String, dislikes: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DIET] = diet
            prefs[Keys.DISLIKES] = dislikes.joinToString(",")
        }
    }

    /** Called after a successful Firebase Auth sign up or log in to update the local cache. */
    suspend fun setAccount(email: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAS_ACCOUNT] = true
            prefs[Keys.IS_GUEST] = false
            prefs[Keys.EMAIL] = email
        }
    }

    suspend fun continueAsGuest() {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_GUEST] = true
        }
    }

    suspend fun logOut() {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_GUEST] = false
            prefs[Keys.HAS_ACCOUNT] = false
            prefs[Keys.PROFILE_SETUP_DONE] = false
        }
    }

    suspend fun savePersonalInfo(info: PersonalInfo) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DISPLAY_NAME] = info.displayName
            prefs[Keys.AGE] = info.age
            prefs[Keys.GENDER] = info.gender
            prefs[Keys.HEIGHT_CM] = info.heightCm
            prefs[Keys.WEIGHT_KG] = info.weightKg
            prefs[Keys.ACTIVITY_LEVEL] = info.activityLevel
        }
    }

    suspend fun saveGoals(goals: Goals) {
        context.dataStore.edit { prefs ->
            prefs[Keys.GOAL_TYPE] = goals.goalType
            prefs[Keys.TARGET_WEIGHT_KG] = goals.targetWeightKg
            prefs[Keys.DAILY_CALORIE_TARGET] = goals.dailyCalorieTarget
            prefs[Keys.DAILY_PROTEIN_TARGET_G] = goals.dailyProteinTargetG
            prefs[Keys.DAILY_WATER_TARGET_L] = goals.dailyWaterTargetL
        }
    }

    suspend fun markProfileSetupDone() {
        context.dataStore.edit { it[Keys.PROFILE_SETUP_DONE] = true }
    }
}
