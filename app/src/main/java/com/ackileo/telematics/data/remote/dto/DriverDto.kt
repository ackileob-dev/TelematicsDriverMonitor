package com.ackileo.telematics.data.remote.dto

import com.google.gson.annotations.SerializedName
data class DriverDto(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String,

    @SerializedName(value = "fullName", alternate = ["full_name", "name"])
    val fullName: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName(value = "phone", alternate = ["phoneNumber", "phone_number"])
    val phone: String?,

    @SerializedName("vehicle")
    val vehicle: VehicleDto? = null
)

