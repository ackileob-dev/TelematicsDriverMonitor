package com.ackileo.telematics.data.remote

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("pagination")
    val pagination: PaginationDto? = null,

    @SerializedName("error")
    val error: ErrorResponse? = null
)

