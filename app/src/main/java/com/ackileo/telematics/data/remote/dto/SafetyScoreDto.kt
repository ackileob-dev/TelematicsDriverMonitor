package com.ackileo.telematics.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SafetyScoreDto(
    @SerializedName(value = "score", alternate = ["safetyScore", "safety_score"])
    val score: Int,

    @SerializedName(value = "computedAt", alternate = ["computed_at", "timestamp"])
    val computedAt: String?,

    @SerializedName("smoothDrivingScore")
    val smoothDrivingScore: Int? = null,

    @SerializedName("speedingScore")
    val speedingScore: Int? = null,

    @SerializedName("focusScore")
    val focusScore: Int? = null,

    @SerializedName("harshBrakingCount")
    val harshBrakingCount: Int? = null,

    @SerializedName("rapidAccelerationCount")
    val rapidAccelerationCount: Int? = null,

    @SerializedName(value = "phoneDistractionCount", alternate = ["phone_distraction_count"])
    val phoneDistractionCount: Int? = null
)

