package com.ackileo.telematics.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TripUpdateRequest(
    @SerializedName("endTimestamp")
    val endTimestamp: String,
    @SerializedName("endLocation")
    val endLocation: TripLocationDto? = null,
    @SerializedName("distance")
    val distance: Double,
    @SerializedName("duration")
    val duration: Int,
    @SerializedName("averageSpeed")
    val averageSpeed: Double,
    @SerializedName("maximumSpeed")
    val maximumSpeed: Double,
    @SerializedName("safetyScore")
    val safetyScore: Double? = null,
)

