package com.example.hikermanagementapp.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.hikermanagementapp.HikerApp
import com.example.hikermanagementapp.R

class ReminderWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val hikeId = inputData.getLong(KEY_HIKE_ID, -1L)
        val hikeName = inputData.getString(KEY_HIKE_NAME) ?: applicationContext.getString(R.string.notification_hike_reminder_default)
        val date = inputData.getString(KEY_HIKE_DATE) ?: ""
        val nm = NotificationManagerCompat.from(applicationContext)
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED || android.os.Build.VERSION.SDK_INT < 33) {
            val notif = NotificationCompat.Builder(applicationContext, HikerApp.CHANNEL_REMINDERS)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(applicationContext.getString(R.string.notification_hike_reminder_title))
                .setContentText("$hikeName on $date")
                .setAutoCancel(true)
                .build()
            nm.notify(hikeId.toInt().coerceAtLeast(1), notif)
        }
        return Result.success()
    }

    companion object {
        const val KEY_HIKE_ID = "hike_id"
        const val KEY_HIKE_NAME = "hike_name"
        const val KEY_HIKE_DATE = "hike_date"
    }
}

