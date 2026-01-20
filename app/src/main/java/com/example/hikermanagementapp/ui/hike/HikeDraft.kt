package com.example.hikermanagementapp.ui.hike

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HikeDraft(
    val id: Long? = null,
    val name: String,
    val location: String,
    val date: String,
    val parkingAvailable: Boolean,
    val lengthKm: Double,
    val difficulty: String,
    val description: String?,
    val elevationGainM: Int?,
    val rating: Float?,
    val photoUri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) : Parcelable
