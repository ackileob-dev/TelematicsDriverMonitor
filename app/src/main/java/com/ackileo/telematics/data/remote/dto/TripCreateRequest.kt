package com.ackileo.telematics.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TripCreateRequest(
    @SerializedName("driverId")
    val driverId: String? = null,
    @SerializedName("vehicleId")
    val vehicleId: String,
    @SerializedName("startTimestamp")
    val startTimestamp: String,
    @SerializedName("startLocation")
    val startLocation: TripLocationDto? = null,
)

