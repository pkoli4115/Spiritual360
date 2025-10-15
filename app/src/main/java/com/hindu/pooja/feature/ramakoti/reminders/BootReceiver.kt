package com.hindu.pooja.feature.ramakoti.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.hindu.pooja.feature.ramakoti.prefs.RamakotiPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED != intent.action) return

        // Work off the receiver thread
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = RamakotiPreferences.getInstance(context)
                val enabled = prefs.reminderEnabled.first()
                if (enabled) {
                    val hour = prefs.reminderHour.first()
                    val minute = prefs.reminderMinute.first()
                    scheduler.scheduleDaily(hour24 = hour, minute = minute)
                } else {
                    // FIX: use the actual API name from ReminderScheduler
                    scheduler.cancelAll()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
