package com.spoonsage.app.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.spoonsage.app.data.UserPrefs
import com.spoonsage.app.data.firebase.FirestoreRepository
import com.spoonsage.app.data.local.AppDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SyncManager(
    private val db: AppDatabase,
    private val prefs: UserPrefs,
    private val firestore: FirestoreRepository
) {
    private val mutex = Mutex()

    private fun currentUid(): String? {
        if (prefs.isGuest) return null
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    suspend fun pendingCount(): Int =
        db.mealPlanDao().getUnsynced().size + db.groceryDao().getUnsynced().size + db.progressDao().getUnsynced().size

    suspend fun syncAll(): Int = mutex.withLock {
        val uid = currentUid() ?: run {
            Log.d(TAG, "Sync skipped - guest or not signed in to Firebase")
            return 0
        }
        var count = 0

        for (entry in db.mealPlanDao().getUnsynced()) {
            try {
                if (entry.pendingDelete) {
                    firestore.deleteMealPlan(uid, entry.id)
                    db.mealPlanDao().hardDelete(entry.id)
                } else {
                    firestore.upsertMealPlan(uid, entry)
                    db.mealPlanDao().markSynced(entry.id)
                }
                count++
            } catch (e: Exception) {
                Log.w(TAG, "Meal plan entry ${entry.id} not synced yet", e)
            }
        }

        for (item in db.groceryDao().getUnsynced()) {
            try {
                if (item.pendingDelete) {
                    firestore.deleteGrocery(uid, item.id)
                    db.groceryDao().hardDelete(item.id)
                } else {
                    firestore.upsertGrocery(uid, item)
                    db.groceryDao().markSynced(item.id)
                }
                count++
            } catch (e: Exception) {
                Log.w(TAG, "Grocery item ${item.id} not synced yet", e)
            }
        }

        for (log in db.progressDao().getUnsynced()) {
            try {
                firestore.upsertProgress(uid, log)
                db.progressDao().markSynced(log.date)
                count++
            } catch (e: Exception) {
                Log.w(TAG, "Progress ${log.date} not synced yet", e)
            }
        }

        try {
            firestore.saveProfile(uid, prefs.toFirestoreMap())
        } catch (e: Exception) {
            Log.w(TAG, "Profile not synced yet", e)
        }

        prefs.lastSyncTime = System.currentTimeMillis()
        Log.d(TAG, "Sync finished: $count change(s) uploaded")
        count
    }

    suspend fun pullAll() {
        val uid = currentUid() ?: return
        syncAll()
        try {
            firestore.getProfile(uid)?.let { prefs.fromFirestoreMap(it) }
            val meals = firestore.getMealPlan(uid)
            val groceries = firestore.getGrocery(uid)
            val progress = firestore.getProgress(uid)
            db.mealPlanDao().clear(); db.mealPlanDao().upsertAll(meals)
            db.groceryDao().clear(); db.groceryDao().upsertAll(groceries)
            db.progressDao().clear(); db.progressDao().upsertAll(progress)
            Log.d(TAG, "Pulled ${meals.size} meals, ${groceries.size} groceries, ${progress.size} logs from Firestore")
        } catch (e: Exception) {
            Log.w(TAG, "Pull from Firestore failed - keeping local data", e)
        }
    }

    companion object {
        private const val TAG = "SyncManager"
    }
}
