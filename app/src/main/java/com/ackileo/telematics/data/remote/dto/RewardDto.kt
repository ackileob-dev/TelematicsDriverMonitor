package com.ackileo.telematics.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RewardDto(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("points")
    val points: Int,

    @SerializedName(value = "issuedAt", alternate = ["issued_at", "createdAt"])
    val issuedAt: String?,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName(value = "isRedeemed", alternate = ["is_redeemed"])
    val isRedeemed: Boolean? = null,

    @SerializedName("available")
    val available: Boolean? = null
)

