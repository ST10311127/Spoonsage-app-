package com.spoonsage.app

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.spoonsage.app.data.UserPrefs
import com.spoonsage.app.data.firebase.AuthRepository
import com.spoonsage.app.data.firebase.FirestoreRepository
import com.spoonsage.app.data.firebase.GeminiCoachClient
import com.spoonsage.app.data.local.AppDatabase
import com.spoonsage.app.data.remote.RetrofitClient
import com.spoonsage.app.data.repository.SpoonSageRepository
import com.spoonsage.app.data.repository.SyncManager
import com.spoonsage.app.notifications.NotificationHelper
import com.spoonsage.app.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SpoonSageApp : Application() {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var prefs: UserPrefs; private set
    lateinit var database: AppDatabase; private set
    lateinit var firestore: FirestoreRepository; private set
    lateinit var authRepository: AuthRepository; private set
    lateinit var syncManager: SyncManager; private set
    lateinit var networkMonitor: NetworkMonitor; private set
    lateinit var repository: SpoonSageRepository; private set

    override fun onCreate() {
        super.onCreate()
        prefs = UserPrefs(this)
        applyTheme(prefs.themeMode)

        database = AppDatabase.get(this)
        firestore = FirestoreRepository()
        authRepository = AuthRepository(database.localUserDao())
        syncManager = SyncManager(database, prefs, firestore)

        networkMonitor = NetworkMonitor(this) { syncAfterReconnect() }
        networkMonitor.start()

        repository = SpoonSageRepository(
            db = database,
            api = RetrofitClient.api,
            prefs = prefs,
            gemini = GeminiCoachClient(),
            syncManager = syncManager,
            appScope = appScope,
            isOnline = { networkMonitor.isConnected() }
        )

        NotificationHelper.createChannels(this)
        appScope.launch { repository.prepopulateSampleData() }
        Log.d(TAG, "SpoonSage started")
    }

    private fun syncAfterReconnect() {
        if (!prefs.autoSync) return
        appScope.launch {
            try {
                val pendingBefore = syncManager.pendingCount()
                val uploaded = syncManager.syncAll()
                Log.d(TAG, "Reconnected: $uploaded of $pendingBefore pending change(s) synced")
                if (uploaded > 0) {
                    NotificationHelper.show(
                        this@SpoonSageApp,
                        R.string.channel_sync_id,
                        getString(R.string.sync_done_title),
                        resources.getQuantityString(R.plurals.sync_done_body, uploaded, uploaded),
                        id = SYNC_NOTIFICATION_ID
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sync after reconnect failed", e)
            }
        }
    }

    companion object {
        private const val TAG = "SpoonSageApp"
        private const val SYNC_NOTIFICATION_ID = 4001

        fun applyTheme(mode: String) {
            AppCompatDelegate.setDefaultNightMode(
                when (mode) {
                    "light" -> AppCompatDelegate.MODE_NIGHT_NO
                    "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            )
        }
    }
}
