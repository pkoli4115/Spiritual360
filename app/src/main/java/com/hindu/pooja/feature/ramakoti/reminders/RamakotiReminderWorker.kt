// FILE: app/src/main/java/com/hindu/pooja/feature/ramakoti/reminders/RamakotiReminderWorker.kt
package com.hindu.pooja.feature.ramakoti.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hindu.pooja.R

class RamakotiReminderWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        createChannel()

        // Android 13+ (Tiramisu, API 33) requires POST_NOTIFICATIONS runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            // If permission not granted, just skip gracefully.
            if (!granted) return Result.success()
        }

        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_ramakoti) // fallback to ic_launcher if missing
            .setContentTitle("Ramakoti reminder")
            .setContentText("A few minutes with “Jai Sri Ram” today?")
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(NOTIF_ID, notif)
        return Result.success()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val ch = NotificationChannel(
                CHANNEL_ID,
                "Ramakoti Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            mgr.createNotificationChannel(ch)
        }
    }

    companion object {
        const val CHANNEL_ID = "ramakoti_reminders"
        const val NOTIF_ID = 1008
    }
}
