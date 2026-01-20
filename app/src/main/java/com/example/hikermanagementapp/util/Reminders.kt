package com.example.hikermanagementapp.util

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object Reminders {
    private const val UNIQUE_PREFIX = "hike_reminder_"

    fun scheduleReminder(context: Context, hikeId: Long, hikeName: String, dateIso: String) {
        try {
            val date = LocalDate.parse(dateIso) // expects yyyy-MM-dd
            val trigger = ZonedDateTime.of(date, LocalTime.of(8, 0), ZoneId.systemDefault())
            val now = ZonedDateTime.now()
            var delayMillis = Duration.between(now, trigger).toMillis()
            if (delayMillis <= 0) {
                // If past time, schedule shortly (1 minute) instead of negative
                delayMillis = TimeUnit.MINUTES.toMillis(1)
            }
            val data = Data.Builder()
                .putLong(ReminderWorker.KEY_HIKE_ID, hikeId)
                .putString(ReminderWorker.KEY_HIKE_NAME, hikeName)
                .putString(ReminderWorker.KEY_HIKE_DATE, dateIso)
                .build()
            val req = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_PREFIX + hikeId,
                ExistingWorkPolicy.REPLACE,
                req
            )
        } catch (_: Exception) {
            // Ignore parsing errors for now
        }
    }
}

