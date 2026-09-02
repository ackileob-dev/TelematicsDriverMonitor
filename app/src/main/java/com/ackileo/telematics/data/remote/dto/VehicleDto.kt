package com.ackileo.telematics.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VehicleDto(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String,

    @SerializedName("make")
    val make: String?,

    @SerializedName("model")
    val model: String?,

    @SerializedName(value = "plateNumber", alternate = ["plate_number", "plate"])
    val plateNumber: String?
)

