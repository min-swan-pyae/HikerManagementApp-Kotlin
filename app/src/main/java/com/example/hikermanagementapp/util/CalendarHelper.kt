package com.example.hikermanagementapp.util

import android.content.Intent
import android.provider.CalendarContract
import com.example.hikermanagementapp.data.Hike
import java.util.Calendar

/**
 * FEATURE G: Additional Feature - Calendar Integration
 *
 * Creates an Intent that opens the calendar app with pre-filled event details.
 *
 * Used in HikeDetailFragment.kt ("Add to Calendar")
 */
object CalendarHelper {

    fun createCalendarIntent(hike: Hike): Intent? {
        // Parse date string "YYYY-MM-DD" into components
        val parts = hike.date.split("-")
        if (parts.size != 3) {
            return null  // Invalid date format
        }

        val year = parts[0].toIntOrNull() ?: return null
        val month = (parts[1].toIntOrNull() ?: 1) - 1  // Calendar months are 0-based
        val day = parts[2].toIntOrNull() ?: return null

        // Create Calendar object for event start time
        val startCal = Calendar.getInstance().apply {
            set(year, month, day, 9, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Build event description with all hike details
        val description = buildString {
            append(hike.description ?: "")
            hike.elevationGainM?.let {
                append("\nElevation gain: ").append(it).append(" m")
            }
            hike.lengthKm.let {
                append("\nLength: ").append(it).append(" km")
            }
            append("\nDifficulty: ").append(hike.difficulty)
        }

        // Create Intent using CalendarContract to interact with calendar app
        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, hike.name)
            putExtra(CalendarContract.Events.EVENT_LOCATION, hike.location)
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startCal.timeInMillis)
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
        }
    }
}
