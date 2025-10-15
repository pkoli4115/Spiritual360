package com.hindu.pooja.feature.ramakoti.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** Schedule a daily reminder at [hour24]:[minute]. */
    fun scheduleDaily(hour24: Int, minute: Int) {
        val atMillis = nextDailyAt(hour24, minute)
        val pi = dailyIntent(context, hour24, minute)
        scheduleExactOrFallback(atMillis, pi)
    }

    /** Cancel all known variants we may have scheduled. */
    fun cancelAll() {
        alarmManager.cancel(dailyIntent(context, 7, 0))
        alarmManager.cancel(weeklyIntent(context, ReminderReceiver.WEEKMASK_ALL, 7, 0))
        alarmManager.cancel(intervalIntent(context, System.currentTimeMillis(), 1, 7, 0))
    }

    // ------------ build intents with the same extras your receiver expects ------------

    private fun dailyIntent(ctx: Context, hour: Int, minute: Int): PendingIntent {
        val i = Intent(ctx, ReminderReceiver::class.java)
            .setAction(ACTION_REMIND)
            .putExtra(ReminderReceiver.EXTRA_TYPE, ReminderReceiver.TYPE_DAILY)
            .putExtra(ReminderReceiver.EXTRA_HOUR, hour)
            .putExtra(ReminderReceiver.EXTRA_MINUTE, minute)
        return pi(ctx, REQ_DAILY, i)
    }

    private fun weeklyIntent(ctx: Context, weekMask: Int, hour: Int, minute: Int): PendingIntent {
        val i = Intent(ctx, ReminderReceiver::class.java)
            .setAction(ACTION_REMIND)
            .putExtra(ReminderReceiver.EXTRA_TYPE, ReminderReceiver.TYPE_WEEKLY)
            .putExtra(ReminderReceiver.EXTRA_WEEKDAYS_MASK, weekMask)
            .putExtra(ReminderReceiver.EXTRA_HOUR, hour)
            .putExtra(ReminderReceiver.EXTRA_MINUTE, minute)
        return pi(ctx, REQ_WEEKLY, i)
    }

    private fun intervalIntent(
        ctx: Context, startEpoch: Long, everyNDays: Int, hour: Int, minute: Int
    ): PendingIntent {
        val i = Intent(ctx, ReminderReceiver::class.java)
            .setAction(ACTION_REMIND)
            .putExtra(ReminderReceiver.EXTRA_TYPE, ReminderReceiver.TYPE_INTERVAL)
            .putExtra(ReminderReceiver.EXTRA_START_EPOCH, startEpoch)
            .putExtra(ReminderReceiver.EXTRA_INTERVAL_DAYS, everyNDays)
            .putExtra(ReminderReceiver.EXTRA_HOUR, hour)
            .putExtra(ReminderReceiver.EXTRA_MINUTE, minute)
        return pi(ctx, REQ_INTERVAL, i)
    }

    private fun pi(ctx: Context, reqCode: Int, intent: Intent): PendingIntent {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        return PendingIntent.getBroadcast(ctx, reqCode, intent, flags)
    }

    // ------------ time calculation & scheduling fallback ------------

    private fun nextDailyAt(hour24: Int, minute: Int): Long {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            set(java.util.Calendar.HOUR_OF_DAY, hour24)
            set(java.util.Calendar.MINUTE, minute)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    private fun scheduleExactOrFallback(atMillis: Long, pi: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pi)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, atMillis, pi)
            }
        } catch (_: SecurityException) {
            // If exact alarms aren’t allowed, keep the chain alive with inexact
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pi)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, atMillis, pi)
            }
        }
    }

    companion object {
        private const val ACTION_REMIND = "com.hindu.pooja.REMIND_TO_WRITE"
        private const val REQ_DAILY = 2001
        private const val REQ_WEEKLY = 2002
        private const val REQ_INTERVAL = 2003
    }
}
