package com.spoonsage.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Syncs the "Account" / "Personal Info" / "Goals" data (Section 7) to
 * Cloud Firestore under users/{uid}, keyed by the signed-in Firebase
 * Auth user. UserPreferences (DataStore) stays as the on-device cache so
 * the app keeps working offline; this repository pushes local changes up
 * and pulls the cloud copy down on login.
 *
 * Firestore's Android SDK queues writes automatically while offline and
 * syncs them once the connection returns, so `syncProfile` is safe to
 * call without checking connectivity first.
 */
class FirestoreUserRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private fun userDoc() = auth.currentUser?.uid?.let {
        firestore.collection("users").document(it)
    }

    fun syncProfile(personalInfo: PersonalInfo, goals: Goals, diet: String, dislikes: List<String>) {
        val doc = userDoc() ?: return
        val data = mapOf(
            "displayName" to personalInfo.displayName,
            "age" to personalInfo.age,
            "gender" to personalInfo.gender,
            "heightCm" to personalInfo.heightCm,
            "weightKg" to personalInfo.weightKg,
            "activityLevel" to personalInfo.activityLevel,
            "goalType" to goals.goalType,
            "targetWeightKg" to goals.targetWeightKg,
            "dailyCalorieTarget" to goals.dailyCalorieTarget,
            "dailyProteinTargetG" to goals.dailyProteinTargetG,
            "dailyWaterTargetL" to goals.dailyWaterTargetL,
            "diet" to diet,
            "dislikes" to dislikes.joinToString(",")
        )
        doc.set(data, SetOptions.merge())
    }

    /** Pulls the cloud copy (if any) into the local DataStore cache, e.g. right after login. */
    suspend fun pullProfileInto(preferences: UserPreferences) {
        val doc = userDoc() ?: return
        try {
            val snapshot = doc.get().await()
            if (!snapshot.exists()) return

            preferences.savePersonalInfo(
                PersonalInfo(
                    displayName = snapshot.getString("displayName") ?: "",
                    age = (snapshot.getLong("age") ?: 0L).toInt(),
                    gender = snapshot.getString("gender") ?: "Prefer not to say",
                    heightCm = (snapshot.getDouble("heightCm") ?: 0.0).toFloat(),
                    weightKg = (snapshot.getDouble("weightKg") ?: 0.0).toFloat(),
                    activityLevel = snapshot.getString("activityLevel") ?: "Medium"
                )
            )
            preferences.saveGoals(
                Goals(
                    goalType = snapshot.getString("goalType") ?: "Eat healthier",
                    targetWeightKg = (snapshot.getDouble("targetWeightKg") ?: 0.0).toFloat(),
                    dailyCalorieTarget = (snapshot.getLong("dailyCalorieTarget") ?: 2200L).toInt(),
                    dailyProteinTargetG = (snapshot.getLong("dailyProteinTargetG") ?: 100L).toInt(),
                    dailyWaterTargetL = (snapshot.getDouble("dailyWaterTargetL") ?: 2.5).toFloat()
                )
            )
            val diet = snapshot.getString("diet") ?: "Classic"
            val dislikes = (snapshot.getString("dislikes") ?: "")
                .split(",").map { it.trim() }.filter { it.isNotEmpty() }
            preferences.saveDietaryPreferences(diet, dislikes)
            preferences.markProfileSetupDone()
        } catch (_: Exception) {
            // Offline or no cloud copy yet (e.g. brand-new account) - keep whatever is cached locally.
        }
    }
}
