package com.ackileo.telematics.data.remote.dto

import com.google.gson.annotations.SerializedName

// Represents a single driving event recorded by sensors
data class DrivingEventDto(
    @SerializedName("type")
    val type: String,

    @SerializedName("timestamp")
    val timestamp: String,

    @SerializedName("latitude")
    val latitude: Double?,

    @SerializedName("longitude")
    val longitude: Double?,

    @SerializedName("value")
    val value: Double? = null
)

