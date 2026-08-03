package com.ackileo.telematics.domain.model



import java.time.LocalDateTime

data class Trip(
    val id: String,
    val vehicleId: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val distanceKm: Double,
    val averageSpeed: Double,
    val startLocationName: String?,
    val endLocationName: String?,
    //new
    val destination: String,
    val date: String,
    val totalDistanceKm: Double,
    val averageSpeedKmh: Double,
    val durationMinutes: Int,
    val safetyScore: Double
)