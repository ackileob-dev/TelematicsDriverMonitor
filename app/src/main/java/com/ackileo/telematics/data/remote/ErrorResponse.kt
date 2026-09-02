package com.ackileo.telematics.data.remote

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("code")
    val code: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("details")
    val details: Map<String, Any?>? = null
)
