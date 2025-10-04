package com.hindu.pooja.feature.ramakoti.reminders

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val appCtx: Context
) {
    companion object { private const val UNIQUE_NAME = "ramakoti_daily_reminder" }

    fun scheduleDaily(hour24: Int, minute: Int) {
        val initialDelay = computeDelayMillis(hour24, minute)
        val req = PeriodicWorkRequestBuilder<RamakotiReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(appCtx)
            .enqueueUniquePeriodicWork(UNIQUE_NAME, ExistingPeriodicWorkPolicy.UPDATE, req)
    }

    fun cancel() {
        WorkManager.getInstance(appCtx).cancelUniqueWork(UNIQUE_NAME)
    }

    private fun computeDelayMillis(h: Int, m: Int): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        return next.timeInMillis - now.timeInMillis
    }
}
