package com.spoonsage.app.data.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.spoonsage.app.data.local.GroceryItem
import com.spoonsage.app.data.local.MealPlanEntry
import com.spoonsage.app.data.local.ProgressLog
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun userDoc(uid: String) = db.collection(USERS).document(uid)

    suspend fun saveProfile(uid: String, data: Map<String, Any>) {
        withTimeout(TIMEOUT_MS) { userDoc(uid).set(data, SetOptions.merge()).await() }
        Log.d(TAG, "Profile saved to Firestore for $uid")
    }

    suspend fun getProfile(uid: String): Map<String, Any?>? =
        withTimeout(TIMEOUT_MS) { userDoc(uid).get().await().data }

    suspend fun saveFcmToken(uid: String, token: String) {
        withTimeout(TIMEOUT_MS) { userDoc(uid).set(mapOf("fcmToken" to token), SetOptions.merge()).await() }
        Log.d(TAG, "FCM token saved")
    }

    suspend fun upsertMealPlan(uid: String, e: MealPlanEntry) = withTimeout(TIMEOUT_MS) {
        userDoc(uid).collection(MEAL_PLAN).document(e.id).set(
            mapOf(
                "recipeId" to e.recipeId,
                "recipeTitle" to e.recipeTitle,
                "recipeImageUrl" to e.recipeImageUrl,
                "dayOfWeek" to e.dayOfWeek,
                "mealType" to e.mealType,
                "calories" to e.calories,
                "updatedAt" to e.updatedAt
            )
        ).await()
    }

    suspend fun deleteMealPlan(uid: String, id: String) = withTimeout(TIMEOUT_MS) {
        userDoc(uid).collection(MEAL_PLAN).document(id).delete().await()
    }

    suspend fun getMealPlan(uid: String): List<MealPlanEntry> = withTimeout(TIMEOUT_MS) {
        userDoc(uid).collection(MEAL_PLAN).get().await().documents.mapNotNull { d ->
            MealPlanEntry(
                id = d.id,
                recipeId = d.getLong("recipeId")?.toInt() ?: return@mapNotNull null,
                recipeTitle = d.getString("recipeTitle") ?: "",
                recipeImageUrl = d.getString("recipeImageUrl"),
                dayOfWeek = d.getString("dayOfWeek") ?: "Monday",
                mealType = d.getString("mealType") ?: "Dinner",
                calories = d.getLong("calories")?.toInt() ?: 0,
                synced = true,
                updatedAt = d.getLong("updatedAt") ?: 0L
            )
        }
    }

    suspend fun upsertGrocery(uid: String, item: GroceryItem) = withTimeout(TIMEOUT_MS) {
        userDoc(uid).collection(GROCERY).document(item.id).set(
            mapOf("name" to item.name, "isChecked" to item.isChecked, "updatedAt" to item.updatedAt)
        ).await()
    }

    suspend fun deleteGrocery(uid: String, id: String) = withTimeout(TIMEOUT_MS) {
        userDoc(uid).collection(GROCERY).document(id).delete().await()
    }

    suspend fun getGrocery(uid: String): List<GroceryItem> = withTimeout(TIMEOUT_MS) {
        userDoc(uid).collection(GROCERY).get().await().documents.map { d ->
            GroceryItem(
                id = d.id,
                name = d.getString("name") ?: "",
                isChecked = d.getBoolean("isChecked") ?: false,
                synced = true,
                updatedAt = d.getLong("updatedAt") ?: 0L
            )
        }
    }

    suspend fun upsertProgress(uid: String, log: ProgressLog) = withTimeout(TIMEOUT_MS) {
        userDoc(uid).collection(PROGRESS).document(log.date).set(
            mapOf(
                "caloriesConsumed" to log.caloriesConsumed,
                "proteinConsumedG" to log.proteinConsumedG,
                "waterConsumedL" to log.waterConsumedL,
                "streakCount" to log.streakCount,
                "updatedAt" to log.updatedAt
            )
        ).await()
    }

    suspend fun getProgress(uid: String): List<ProgressLog> = withTimeout(TIMEOUT_MS) {
        userDoc(uid).collection(PROGRESS).get().await().documents.map { d ->
            ProgressLog(
                date = d.id,
                caloriesConsumed = d.getLong("caloriesConsumed")?.toInt() ?: 0,
                proteinConsumedG = d.getLong("proteinConsumedG")?.toInt() ?: 0,
                waterConsumedL = d.getDouble("waterConsumedL") ?: 0.0,
                streakCount = d.getLong("streakCount")?.toInt() ?: 0,
                synced = true,
                updatedAt = d.getLong("updatedAt") ?: 0L
            )
        }
    }

    companion object {
        private const val TAG = "FirestoreRepository"
        private const val USERS = "users"
        private const val MEAL_PLAN = "mealPlan"
        private const val GROCERY = "grocery"
        private const val PROGRESS = "progress"
        private const val TIMEOUT_MS = 15_000L
    }
}
