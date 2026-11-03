package com.spoonsage.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.spoonsage.app.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.get(context).reminderDao().getAll().forEach { ReminderScheduler.schedule(context, it) }
                Log.d("BootReceiver", "Reminders re-scheduled after reboot")
            } finally {
                pending.finish()
            }
        }
    }
}
