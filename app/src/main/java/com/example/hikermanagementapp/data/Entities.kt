package com.example.hikermanagementapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "hikes",
    indices = [Index(value = ["name"], unique = false)]
)
data class Hike(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val location: String,
    val date: String,
    val parkingAvailable: Boolean,
    val lengthKm: Double,
    val difficulty: String,
    val description: String? = null,
    val elevationGainM: Int? = null,
    val rating: Float? = null,
    val photoUri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val addedToCalendar: Boolean = false
)


@Entity(
    tableName = "observations",
    foreignKeys = [
        ForeignKey(
            entity = Hike::class,
            parentColumns = ["id"],
            childColumns = ["hikeId"],
            onDelete = ForeignKey.CASCADE,  // FEATURE B: Cascade delete observations when hike is deleted
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["hikeId"])],
)
data class Observation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hikeId: Long,  // Foreign key to Hike
    val observation: String,
    val timestamp: Long,  // Stored as milliseconds (System.currentTimeMillis())
    val comments: String? = null,
    val photoUri: String? = null
)
