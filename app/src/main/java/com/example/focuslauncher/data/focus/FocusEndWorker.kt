package com.example.focuslauncher.data.focus

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.focuslauncher.MainActivity
import com.example.focuslauncher.R

/**
 * Plain Worker (no Hilt): fires at the planned session end and posts a
 * gentle summary notification. Tapping it opens the app, whose startup
 * refresh() closes the expired session row. Approximate timing under Doze.
 */
class FocusEndWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val profile = inputData.getString(KEY_PROFILE) ?: return Result.success()
        val minutes = inputData.getInt(KEY_MINUTES, 0)
        val nm = applicationContext.getSystemService(NotificationManager::class.java)
            ?: return Result.success()
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Focus", NotificationManager.IMPORTANCE_DEFAULT),
        )
        val open = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val text = applicationContext.getString(R.string.focus_end_notification, minutes, profile)
        nm.notify(
            NOTIFICATION_ID,
            NotificationCompat.Builder(applicationContext, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(applicationContext.getString(R.string.focus_end_title))
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(open)
                .setAutoCancel(true)
                .build(),
        )
        return Result.success()
    }

    companion object {
        const val CHANNEL = "focus"
        const val NOTIFICATION_ID = 1001
        const val KEY_PROFILE = "profile"
        const val KEY_MINUTES = "minutes"
    }
}
