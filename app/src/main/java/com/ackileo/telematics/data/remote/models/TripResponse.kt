package com.ackileo.telematics.data.remote.models



import com.google.gson.annotations.SerializedName

/**
 * Data model representing the Trip response from the remote API.
 */
data class TripResponse(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("start_time")
    val startTime: String? = null,

    @SerializedName("end_time")
    val endTime: String? = null,

    @SerializedName("total_distance")
    val totalDistance: Double? = null,

    @SerializedName("average_speed")
    val averageSpeed: Double? = null,

    @SerializedName("duration")
    val duration: Int? = null, // In minutes

    @SerializedName("safety_score")
    val safetyScore: Double? = null,

    @SerializedName("destination")
    val destination: String? = null,

    @SerializedName("status")
    val status: String? = null // e.g., "COMPLETED", "ONGOING"
)