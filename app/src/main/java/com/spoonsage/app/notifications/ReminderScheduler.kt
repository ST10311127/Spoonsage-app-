package com.spoonsage.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.spoonsage.app.data.local.Reminder
import java.util.Calendar

object ReminderScheduler {

    private const val TAG = "ReminderScheduler"
    const val EXTRA_TYPE = "reminder_type"

    fun schedule(context: Context, reminder: Reminder) {
        if (!reminder.isEnabled) {
            cancel(context, reminder)
            return
        }
        val parts = reminder.time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return

        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            trigger.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent(context, reminder)
        )
        Log.d(TAG, "Scheduled ${reminder.type} reminder at ${reminder.time}")
    }

    fun cancel(context: Context, reminder: Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context, reminder))
        Log.d(TAG, "Cancelled reminder ${reminder.id}")
    }

    private fun pendingIntent(context: Context, reminder: Reminder): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            reminder.id,
            Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_TYPE, reminder.type),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
}
