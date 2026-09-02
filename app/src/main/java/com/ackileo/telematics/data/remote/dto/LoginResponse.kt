package com.ackileo.telematics.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName(value = "token", alternate = ["accessToken", "access_token"])
    val token: String,

    @SerializedName(value = "driverId", alternate = ["driver_id"])
    val driverId: String?,

    @SerializedName(value = "fullName", alternate = ["full_name", "name"])
    val fullName: String?
)

