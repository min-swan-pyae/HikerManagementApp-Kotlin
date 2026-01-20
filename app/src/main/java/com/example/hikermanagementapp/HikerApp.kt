package com.example.hikermanagementapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.google.android.material.color.DynamicColors

class HikerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        // Enable dynamic color if available (Android 12+)
        DynamicColors.applyToActivitiesIfAvailable(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_REMINDERS,
            "Hike Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for upcoming hikes"
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_REMINDERS = "mhike_reminders"
        lateinit var instance: HikerApp
            private set
    }
}
