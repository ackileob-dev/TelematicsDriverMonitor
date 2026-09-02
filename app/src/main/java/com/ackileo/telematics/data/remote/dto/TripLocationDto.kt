package com.ackileo.telematics.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TripLocationDto(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("accuracy")
    val accuracy: Float? = null,
)

