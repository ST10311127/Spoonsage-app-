package com.spoonsage.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.spoonsage.app.R
import com.spoonsage.app.data.UserPrefs
import com.spoonsage.app.ui.SplashActivity

object NotificationHelper {

    private const val TAG = "NotificationHelper"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                context.getString(R.string.channel_push_id),
                context.getString(R.string.channel_push_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = context.getString(R.string.channel_push_desc) }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                context.getString(R.string.channel_reminder_id),
                context.getString(R.string.channel_reminder_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
        manager.createNotificationChannel(
            NotificationChannel(
                context.getString(R.string.channel_sync_id),
                context.getString(R.string.channel_sync_name),
                NotificationManager.IMPORTANCE_LOW
            )
        )
    }

    fun show(context: Context, channelIdRes: Int, title: String, body: String, id: Int = System.currentTimeMillis().toInt()) {
        if (!UserPrefs(context).notificationsEnabled) {
            Log.d(TAG, "Notifications turned off in Settings - skipped '$title'")
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted")
            return
        }
        val openApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, SplashActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, context.getString(channelIdRes))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .setColor(ContextCompat.getColor(context, R.color.sage_green))
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
        Log.d(TAG, "Notification shown: $title")
    }
}
