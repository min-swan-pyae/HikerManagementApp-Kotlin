package com.example.hikermanagementapp.util

import android.content.Context
import com.example.hikermanagementapp.R
import com.example.hikermanagementapp.data.Hike
import com.example.hikermanagementapp.data.Observation
import org.json.JSONArray
import org.json.JSONObject

/**
 * Centralized manager for JSON import/export functionality
 */
class ImportExportManager(private val context: Context) {

    fun parseImportJson(jsonText: String): ParsedHikeData? {
        try {
            // Remove any text before the JSON if present
            val cleanJson = if (jsonText.contains("=== JSON Data (for import) ===")) {
                val startIndex = jsonText.indexOf("{")
                if (startIndex != -1) jsonText.substring(startIndex) else jsonText
            } else {
                jsonText
            }

            val obj = JSONObject(cleanJson.trim())

            val name = obj.getString("name")
            val location = obj.getString("location")
            val date = obj.getString("date")
            val parking = obj.optBoolean("parkingAvailable", false)
            val lengthKm = obj.getDouble("lengthKm")
            val difficulty = obj.getString("difficulty")
            val description = obj.optString("description").takeIf { it.isNotBlank() }
            val elevation = if (obj.has("elevationGainM") && !obj.isNull("elevationGainM")) {
                obj.getInt("elevationGainM")
            } else null
            val rating = if (obj.has("rating") && !obj.isNull("rating")) {
                obj.getDouble("rating").toFloat()
            } else null
            val latitude = if (obj.has("latitude") && !obj.isNull("latitude")) {
                obj.getDouble("latitude")
            } else null
            val longitude = if (obj.has("longitude") && !obj.isNull("longitude")) {
                obj.getDouble("longitude")
            } else null

            // Parse observations
            val observations = mutableListOf<ObservationData>()
            val obsArr: JSONArray? = obj.optJSONArray("observations")
            if (obsArr != null) {
                for (i in 0 until obsArr.length()) {
                    val o = obsArr.optJSONObject(i) ?: continue
                    observations.add(
                        ObservationData(
                            observation = o.optString("observation"),
                            timestamp = o.optLong("timestamp", System.currentTimeMillis()),
                            comments = o.optString("comments").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }

            return ParsedHikeData(
                name = name,
                location = location,
                date = date,
                parkingAvailable = parking,
                lengthKm = lengthKm,
                difficulty = difficulty,
                description = description,
                elevationGainM = elevation,
                rating = rating,
                latitude = latitude,
                longitude = longitude,
                observations = observations
            )
        } catch (e: Exception) {
            return null
        }
    }

    fun createExportText(hike: Hike, observations: List<Observation>): String {
        val summary = buildString {
            appendLine("=== Hike Export ===")
            appendLine("Name: ${hike.name}")
            appendLine("Location: ${hike.location}")
            appendLine("Date: ${hike.date}")
            appendLine("Length: ${hike.lengthKm} km")
            appendLine("Difficulty: ${hike.difficulty}")
            val parkingText = if (hike.parkingAvailable) {
                context.getString(R.string.value_available)
            } else {
                context.getString(R.string.value_unavailable)
            }
            appendLine("Parking: $parkingText")
            hike.elevationGainM?.let { appendLine("Elevation Gain: $it m") }
            hike.rating?.let { appendLine("Rating: $it/5") }
            hike.description?.let { appendLine("Description: $it") }
            if (observations.isNotEmpty()) {
                appendLine("\nObservations: ${observations.size}")
            }
            appendLine("\n=== JSON Data (for import) ===")
        }

        val json = JSONObject().apply {
            put("name", hike.name)
            put("location", hike.location)
            put("date", hike.date)
            put("parkingAvailable", hike.parkingAvailable)
            put("lengthKm", hike.lengthKm)
            put("difficulty", hike.difficulty)
            if (!hike.description.isNullOrBlank()) put("description", hike.description)
            hike.elevationGainM?.let { put("elevationGainM", it) }
            hike.rating?.let { put("rating", it) }
            hike.latitude?.let { put("latitude", it) }
            hike.longitude?.let { put("longitude", it) }

            // Add observations
            val arr = JSONArray()
            observations.forEach { obs ->
                val jo = JSONObject().apply {
                    put("observation", obs.observation)
                    put("timestamp", obs.timestamp)
                    obs.comments?.let { put("comments", it) }
                }
                arr.put(jo)
            }
            put("observations", arr)
        }

        return summary + json.toString(2)
    }

    /**
     * Convert parsed data to Hike entity
     */
    fun toHikeEntity(data: ParsedHikeData): Hike {
        return Hike(
            id = 0,
            name = data.name,
            location = data.location,
            date = data.date,
            parkingAvailable = data.parkingAvailable,
            lengthKm = data.lengthKm,
            difficulty = data.difficulty,
            description = data.description,
            elevationGainM = data.elevationGainM,
            rating = data.rating,
            photoUri = null,
            latitude = data.latitude,
            longitude = data.longitude,
            addedToCalendar = false
        )
    }

    /**
     * Convert observation data to Observation entity
     */
    fun toObservationEntity(data: ObservationData, hikeId: Long): Observation {
        return Observation(
            id = 0,
            hikeId = hikeId,
            observation = data.observation,
            timestamp = data.timestamp,
            comments = data.comments,
            photoUri = null
        )
    }

    data class ParsedHikeData(
        val name: String,
        val location: String,
        val date: String,
        val parkingAvailable: Boolean,
        val lengthKm: Double,
        val difficulty: String,
        val description: String?,
        val elevationGainM: Int?,
        val rating: Float?,
        val latitude: Double?,
        val longitude: Double?,
        val observations: List<ObservationData>
    )

    data class ObservationData(
        val observation: String,
        val timestamp: Long,
        val comments: String?
    )
}

