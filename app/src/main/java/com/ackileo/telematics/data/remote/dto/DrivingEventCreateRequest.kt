package com.ackileo.telematics.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DrivingEventCreateRequest(
	@SerializedName("tripId")
	val tripId: String,
	@SerializedName("driverId")
	val driverId: String? = null,
	@SerializedName("type")
	val type: String,
	@SerializedName("timestamp")
	val timestamp: String,
	@SerializedName("latitude")
	val latitude: Double,
	@SerializedName("longitude")
	val longitude: Double,
	@SerializedName("gpsSpeed")
	val gpsSpeed: Double,
	@SerializedName("accuracy")
	val accuracy: Float,
	@SerializedName("accelerometerX")
	val accelerometerX: Float? = null,
	@SerializedName("accelerometerY")
	val accelerometerY: Float? = null,
	@SerializedName("accelerometerZ")
	val accelerometerZ: Float? = null,
	@SerializedName("severity")
	val severity: String,
)

