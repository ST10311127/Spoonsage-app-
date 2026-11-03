package com.spoonsage.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.spoonsage.app.util.Options

class UserPrefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("spoonsage_prefs", Context.MODE_PRIVATE)

    var isGuest: Boolean
        get() = prefs.getBoolean(KEY_IS_GUEST, false)
        set(v) = prefs.edit { putBoolean(KEY_IS_GUEST, v) }

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_LOGGED_IN, false)
        set(v) = prefs.edit { putBoolean(KEY_LOGGED_IN, v) }

    var uid: String?
        get() = prefs.getString(KEY_UID, null)
        set(v) = prefs.edit { putString(KEY_UID, v) }

    var profileSetupDone: Boolean
        get() = prefs.getBoolean(KEY_SETUP_DONE, false)
        set(v) = prefs.edit { putBoolean(KEY_SETUP_DONE, v) }

    var displayName: String
        get() = prefs.getString(KEY_NAME, "") ?: ""
        set(v) = prefs.edit { putString(KEY_NAME, v) }
    var email: String
        get() = prefs.getString(KEY_EMAIL, "") ?: ""
        set(v) = prefs.edit { putString(KEY_EMAIL, v) }
    var age: Int
        get() = prefs.getInt(KEY_AGE, 25)
        set(v) = prefs.edit { putInt(KEY_AGE, v) }
    var gender: String
        get() = prefs.getString(KEY_GENDER, Options.GENDER_NONE) ?: Options.GENDER_NONE
        set(v) = prefs.edit { putString(KEY_GENDER, v) }
    var heightCm: Float
        get() = prefs.getFloat(KEY_HEIGHT, 170f)
        set(v) = prefs.edit { putFloat(KEY_HEIGHT, v) }
    var weightKg: Float
        get() = prefs.getFloat(KEY_WEIGHT, 70f)
        set(v) = prefs.edit { putFloat(KEY_WEIGHT, v) }
    var activityLevel: String
        get() = prefs.getString(KEY_ACTIVITY, Options.ACTIVITY_MEDIUM) ?: Options.ACTIVITY_MEDIUM
        set(v) = prefs.edit { putString(KEY_ACTIVITY, v) }

    var goalType: String
        get() = prefs.getString(KEY_GOAL, Options.GOAL_EAT_HEALTHIER) ?: Options.GOAL_EAT_HEALTHIER
        set(v) = prefs.edit { putString(KEY_GOAL, v) }

    var targetWeightKg: Float
        get() = prefs.getFloat(KEY_TARGET_WEIGHT, 0f)
        set(v) = prefs.edit { putFloat(KEY_TARGET_WEIGHT, v) }
    var dailyCalorieTarget: Int
        get() = prefs.getInt(KEY_CAL_TARGET, 2000)
        set(v) = prefs.edit { putInt(KEY_CAL_TARGET, v) }
    var dailyProteinTargetG: Int
        get() = prefs.getInt(KEY_PROTEIN_TARGET, 80)
        set(v) = prefs.edit { putInt(KEY_PROTEIN_TARGET, v) }
    var dailyWaterTargetL: Float
        get() = prefs.getFloat(KEY_WATER_TARGET, 2.0f)
        set(v) = prefs.edit { putFloat(KEY_WATER_TARGET, v) }

    var diet: String
        get() = prefs.getString(KEY_DIET, "Classic") ?: "Classic"
        set(v) = prefs.edit { putString(KEY_DIET, v) }
    var dislikes: Set<String>
        get() = prefs.getStringSet(KEY_DISLIKES, emptySet())?.toSet() ?: emptySet()
        set(v) = prefs.edit { putStringSet(KEY_DISLIKES, v) }

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS, true)
        set(v) = prefs.edit { putBoolean(KEY_NOTIFICATIONS, v) }

    var themeMode: String
        get() = prefs.getString(KEY_THEME, "system") ?: "system"
        set(v) = prefs.edit { putString(KEY_THEME, v) }
    var autoSync: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SYNC, true)
        set(v) = prefs.edit { putBoolean(KEY_AUTO_SYNC, v) }
    var lastSyncTime: Long
        get() = prefs.getLong(KEY_LAST_SYNC, 0L)
        set(v) = prefs.edit { putLong(KEY_LAST_SYNC, v) }
    var sampleDataLoaded: Boolean
        get() = prefs.getBoolean(KEY_SAMPLES, false)
        set(v) = prefs.edit { putBoolean(KEY_SAMPLES, v) }

    fun coachContext(): String =
        "Name: ${displayName.ifBlank { "User" }}; age $age; $gender; ${heightCm.toInt()} cm; " +
            "${weightKg} kg; activity $activityLevel; goal: $goalType; diet: $diet; " +
            "avoids: ${dislikes.joinToString().ifBlank { "nothing" }}; " +
            "daily targets: $dailyCalorieTarget kcal, ${dailyProteinTargetG} g protein, ${dailyWaterTargetL} L water."

    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "displayName" to displayName,
        "email" to email,
        "age" to age,
        "gender" to gender,
        "heightCm" to heightCm.toDouble(),
        "weightKg" to weightKg.toDouble(),
        "activityLevel" to activityLevel,
        "goalType" to goalType,
        "targetWeightKg" to targetWeightKg.toDouble(),
        "diet" to diet,
        "dislikes" to dislikes.toList(),
        "dailyCalorieTarget" to dailyCalorieTarget,
        "dailyProteinTargetG" to dailyProteinTargetG,
        "dailyWaterTargetL" to dailyWaterTargetL.toDouble(),
        "profileSetupDone" to profileSetupDone,
        "updatedAt" to System.currentTimeMillis()
    )

    fun fromFirestoreMap(map: Map<String, Any?>) {
        (map["displayName"] as? String)?.takeIf { it.isNotBlank() }?.let { displayName = it }
        (map["age"] as? Number)?.let { age = it.toInt() }
        (map["gender"] as? String)?.let { gender = it }
        (map["heightCm"] as? Number)?.let { heightCm = it.toFloat() }
        (map["weightKg"] as? Number)?.let { weightKg = it.toFloat() }
        (map["activityLevel"] as? String)?.let { activityLevel = it }
        (map["goalType"] as? String)?.let { goalType = it }
        (map["targetWeightKg"] as? Number)?.let { targetWeightKg = it.toFloat() }
        (map["diet"] as? String)?.let { diet = it }
        (map["dislikes"] as? List<*>)?.let { list -> dislikes = list.filterIsInstance<String>().toSet() }
        (map["dailyCalorieTarget"] as? Number)?.let { dailyCalorieTarget = it.toInt() }
        (map["dailyProteinTargetG"] as? Number)?.let { dailyProteinTargetG = it.toInt() }
        (map["dailyWaterTargetL"] as? Number)?.let { dailyWaterTargetL = it.toFloat() }
        (map["profileSetupDone"] as? Boolean)?.let { profileSetupDone = it }
    }

    fun clearSession() {
        val keepTheme = themeMode
        val keepNotifications = notificationsEnabled
        prefs.edit { clear() }
        themeMode = keepTheme
        notificationsEnabled = keepNotifications
    }

    companion object {
        private const val KEY_IS_GUEST = "is_guest"
        private const val KEY_LOGGED_IN = "has_account"
        private const val KEY_UID = "uid"
        private const val KEY_SETUP_DONE = "profile_setup_done"
        private const val KEY_NAME = "display_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_AGE = "age"
        private const val KEY_GENDER = "gender"
        private const val KEY_HEIGHT = "height_cm"
        private const val KEY_WEIGHT = "weight_kg"
        private const val KEY_ACTIVITY = "activity_level"
        private const val KEY_GOAL = "goal_type"
        private const val KEY_TARGET_WEIGHT = "target_weight_kg"
        private const val KEY_CAL_TARGET = "daily_calorie_target"
        private const val KEY_PROTEIN_TARGET = "daily_protein_target_g"
        private const val KEY_WATER_TARGET = "daily_water_target_l"
        private const val KEY_DIET = "diet"
        private const val KEY_DISLIKES = "dislikes"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_AUTO_SYNC = "auto_sync"
        private const val KEY_LAST_SYNC = "last_sync"
        private const val KEY_SAMPLES = "sample_data_loaded"
    }
}
