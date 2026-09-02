package com.ackileo.telematics.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AlertDto(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("level")
    val level: String?, // e.g. "info", "warning", "critical"

    @SerializedName("timestamp")
    val timestamp: String?,

    @SerializedName(value = "isRead", alternate = ["is_read"])
    val isRead: Boolean? = null,

    @SerializedName("read")
    val read: Boolean? = null
)

