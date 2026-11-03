package com.spoonsage.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.spoonsage.app.R
import com.spoonsage.app.util.Options

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val (title, body) = when (intent.getStringExtra(ReminderScheduler.EXTRA_TYPE)) {
            Options.REMINDER_WATER -> R.string.reminder_water_title to R.string.reminder_water_body
            Options.REMINDER_MEAL -> R.string.reminder_meal_title to R.string.reminder_meal_body
            else -> R.string.reminder_log_title to R.string.reminder_log_body
        }
        NotificationHelper.show(context, R.string.channel_reminder_id, context.getString(title), context.getString(body))
    }
}
