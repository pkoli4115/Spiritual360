package com.hindu.pooja.feature.ramakoti.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            // Re-schedule with a sensible default (7:00). If you already persist time in DataStore,
            // you can read it here and schedule with the saved hour/minute.
            scheduler.scheduleDaily(7, 0)
        }
    }
}
