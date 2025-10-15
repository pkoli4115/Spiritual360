package com.hindu.pooja.feature.ramakoti.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.hindu.pooja.R
import java.util.Calendar

/**
 * Receives the alarm, shows the notification, and schedules the NEXT one
 * based purely on intent extras (no direct prefs access here).
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 1) Read schedule params from extras (all have safe defaults)
        val type = intent.getStringExtra(EXTRA_TYPE) ?: TYPE_DAILY

        val hour = intent.getIntExtra(EXTRA_HOUR, 7)
        val minute = intent.getIntExtra(EXTRA_MINUTE, 0)

        // WEEKLY: bitmask where Mon=1, Tue=2, ..., Sun=7
        val weekMask = intent.getIntExtra(EXTRA_WEEKDAYS_MASK, WEEKMASK_MON_FRI)

        // INTERVAL: every N days, starting from startEpochMs
        val startEpochMs = intent.getLongExtra(EXTRA_START_EPOCH, System.currentTimeMillis())
        val everyNDays   = intent.getIntExtra(EXTRA_INTERVAL_DAYS, 1).coerceAtLeast(1)

        // ONE-TIME: exact epochMs (unused here because we don’t reschedule one-time)
        val oneTimeAtMs  = intent.getLongExtra(EXTRA_ONE_TIME_AT, 0L)

        // 2) Show notification (guard POST_NOTIFICATIONS on Android 13+)
        showReminderNotification(context)

        // 3) Schedule the NEXT alarm according to the same rule
        when (type) {
            TYPE_ONE_TIME -> {
                // Do nothing (one-shot). If you want “snooze”, schedule it here.
            }
            TYPE_DAILY -> {
                val next = nextDailyAt(hour, minute)
                scheduleExactOrFallback(context, next, dailyIntent(context, hour, minute))
            }
            TYPE_WEEKLY -> {
                val next = nextWeeklyAt(weekMask, hour, minute)
                scheduleExactOrFallback(context, next, weeklyIntent(context, weekMask, hour, minute))
            }
            TYPE_INTERVAL -> {
                val next = nextIntervalFrom(startEpochMs, everyNDays, hour, minute)
                scheduleExactOrFallback(context, next, intervalIntent(context, startEpochMs, everyNDays, hour, minute))
            }
            else -> {
                // Fallback to daily
                val next = nextDailyAt(hour, minute)
                scheduleExactOrFallback(context, next, dailyIntent(context, hour, minute))
            }
        }
    }

    // ------------ Notification ------------

    private fun showReminderNotification(context: Context) {
        val nm = NotificationManagerCompat.from(context)
        ensureChannel(context)

        // Android 13+ permission guard
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        // Open app when tapping the notification (improves perceived “it worked”)
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val contentPi = if (launchIntent != null) {
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
            PendingIntent.getActivity(context, 9901, launchIntent, flags)
        } else null

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // keep your safe fallback icon
            .setContentTitle("Ramakoti Reminder")
            .setContentText("Time to write Jai Sri Ram.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentPi)
            .build()

        nm.notify(NOTIF_ID, notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                val ch = NotificationChannel(
                    CHANNEL_ID,
                    "Ramakoti Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Daily/weekly/interval reminders to continue writing Jai Sri Ram"
                    enableLights(true)
                    lightColor = Color.MAGENTA
                }
                mgr.createNotificationChannel(ch)
            }
        }
    }

    // ------------ Next time calculators (Calendar; compatible with minSdk 24) ------------

    private fun nextDailyAt(hour24: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, minute)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    /**
     * weekMask uses bits for days: 1=Mon, 2=Tue, 4=Wed, 8=Thu, 16=Fri, 32=Sat, 64=Sun
     */
    private fun nextWeeklyAt(weekMask: Int, hour24: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, minute)
        }

        // Map Calendar.DAY_OF_WEEK(1..7) -> Mon(1)..Sun(7)
        fun toMonFirst(dow: Int): Int = when (dow) {
            Calendar.MONDAY    -> 1
            Calendar.TUESDAY   -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY  -> 4
            Calendar.FRIDAY    -> 5
            Calendar.SATURDAY  -> 6
            else               -> 7 // Sunday
        }
        fun isEnabled(monFirst: Int): Boolean = ((weekMask shr (monFirst - 1)) and 1) == 1

        // Try today; if past or not enabled, advance up to 7 days
        repeat(8) {
            val monFirst = toMonFirst(cal.get(Calendar.DAY_OF_WEEK))
            if (isEnabled(monFirst) && cal.timeInMillis > System.currentTimeMillis()) {
                return cal.timeInMillis
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
            cal.set(Calendar.HOUR_OF_DAY, hour24)
            cal.set(Calendar.MINUTE, minute)
        }
        // Fallback – one day later
        return nextDailyAt(hour24, minute)
    }

    private fun nextIntervalFrom(startEpochMs: Long, everyNDays: Int, hour24: Int, minute: Int): Long {
        val startCal = Calendar.getInstance().apply {
            timeInMillis = startEpochMs
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, minute)
        }
        var candidate = startCal.timeInMillis
        val stepMs = everyNDays.toLong() * 24L * 60L * 60L * 1000L
        val now = System.currentTimeMillis()
        while (candidate <= now) candidate += stepMs
        return candidate
    }

    // ------------ Scheduling helpers ------------

    /**
     * Try exact alarm; if denied (Android 12+ without SCHEDULE_EXACT_ALARM),
     * gracefully fall back to inexact so the chain continues.
     */
    private fun scheduleExactOrFallback(context: Context, atMillis: Long, pi: PendingIntent) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pi)
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, atMillis, pi)
            }
        } catch (_: SecurityException) {
            // Fall back to inexact; better to notify roughly than to stop scheduling.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pi)
            } else {
                am.set(AlarmManager.RTC_WAKEUP, atMillis, pi)
            }
        }
    }

    // Build PendingIntents that include all data needed for the NEXT schedule

    private fun baseIntent(context: Context): Intent =
        Intent(context, ReminderReceiver::class.java).setAction(ACTION_REMIND)

    private fun dailyIntent(context: Context, hour: Int, minute: Int): PendingIntent {
        val i = baseIntent(context)
            .putExtra(EXTRA_TYPE, TYPE_DAILY)
            .putExtra(EXTRA_HOUR, hour)
            .putExtra(EXTRA_MINUTE, minute)
        return pi(context, REQ_DAILY, i)
    }

    private fun weeklyIntent(context: Context, weekMask: Int, hour: Int, minute: Int): PendingIntent {
        val i = baseIntent(context)
            .putExtra(EXTRA_TYPE, TYPE_WEEKLY)
            .putExtra(EXTRA_WEEKDAYS_MASK, weekMask)
            .putExtra(EXTRA_HOUR, hour)
            .putExtra(EXTRA_MINUTE, minute)
        return pi(context, REQ_WEEKLY, i)
    }

    private fun intervalIntent(context: Context, startEpoch: Long, everyNDays: Int, hour: Int, minute: Int): PendingIntent {
        val i = baseIntent(context)
            .putExtra(EXTRA_TYPE, TYPE_INTERVAL)
            .putExtra(EXTRA_START_EPOCH, startEpoch)
            .putExtra(EXTRA_INTERVAL_DAYS, everyNDays)
            .putExtra(EXTRA_HOUR, hour)
            .putExtra(EXTRA_MINUTE, minute)
        return pi(context, REQ_INTERVAL, i)
    }

    private fun pi(context: Context, reqCode: Int, intent: Intent): PendingIntent {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
        return PendingIntent.getBroadcast(context, reqCode, intent, flags)
    }

    companion object {
        // Notification
        private const val CHANNEL_ID = "ramakoti_reminders"
        private const val NOTIF_ID = 1001

        // Intent action + extras
        private const val ACTION_REMIND = "com.hindu.pooja.REMIND_TO_WRITE"

        const val EXTRA_TYPE = "type"                 // String: one of TYPE_*
        const val EXTRA_HOUR = "hour"                 // Int (24h)
        const val EXTRA_MINUTE = "minute"             // Int
        const val EXTRA_WEEKDAYS_MASK = "week_mask"   // Int bitmask for weekly
        const val EXTRA_START_EPOCH = "start_epoch"   // Long (for interval)
        const val EXTRA_INTERVAL_DAYS = "every_n_days"// Int (for interval)
        const val EXTRA_ONE_TIME_AT = "one_time_at"   // Long (for one-time)

        // Types (strings for extras to avoid enum reflection in receivers)
        const val TYPE_ONE_TIME = "ONE_TIME"
        const val TYPE_DAILY    = "DAILY"
        const val TYPE_WEEKLY   = "WEEKLY"
        const val TYPE_INTERVAL = "INTERVAL"

        // Request codes for different rule variants
        private const val REQ_DAILY    = 2001
        private const val REQ_WEEKLY   = 2002
        private const val REQ_INTERVAL = 2003

        // Handy default masks
        const val WEEKMASK_MON_FRI = (1 shl 0) or (1 shl 1) or (1 shl 2) or (1 shl 3) or (1 shl 4)
        const val WEEKMASK_ALL     = (1 shl 0) or (1 shl 1) or (1 shl 2) or (1 shl 3) or (1 shl 4) or (1 shl 5) or (1 shl 6)
    }
}
