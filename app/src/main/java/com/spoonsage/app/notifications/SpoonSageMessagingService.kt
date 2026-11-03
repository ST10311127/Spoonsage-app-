package com.spoonsage.app.notifications

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.spoonsage.app.R
import com.spoonsage.app.data.firebase.FirestoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SpoonSageMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        Log.d(TAG, "New FCM token: $token")
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        scope.launch {
            try {
                FirestoreRepository().saveFcmToken(uid, token)
            } catch (e: Exception) {
                Log.w(TAG, "Could not save FCM token", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "Push received from ${message.from}: ${message.data}")
        val title = message.notification?.title ?: message.data["title"] ?: getString(R.string.app_name)
        val body = message.notification?.body ?: message.data["body"] ?: return
        NotificationHelper.show(this, R.string.channel_push_id, title, body)
    }

    companion object {
        private const val TAG = "FCMService"

        const val TOPIC_TIPS = "spoonsage_tips"
    }
}
