package com.ackileo.telematics.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TripDto(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String,

    // Nullable so that in-progress trips (missing endTime / safetyScore) do not
    // cause a Gson JsonSyntaxException when the backend omits these fields.
    @SerializedName(value = "startTime", alternate = ["start_time", "startTimestamp"])
    val startTime: String? = null,

    @SerializedName(value = "endTime", alternate = ["end_time", "endTimestamp"])
    val endTime: String? = null,

    @SerializedName(value = "totalDistance", alternate = ["total_distance", "distance"])
    val totalDistance: Double? = null,

    @SerializedName(value = "averageSpeed", alternate = ["average_speed"])
    val averageSpeed: Double? = null,

    @SerializedName("duration")
    val duration: Int? = null,

    @SerializedName(value = "safetyScore", alternate = ["safety_score"])
    val safetyScore: Double? = null
)

