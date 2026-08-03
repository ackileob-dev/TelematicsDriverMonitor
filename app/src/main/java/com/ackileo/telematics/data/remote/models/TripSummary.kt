

package com.ackileo.telematics.data.remote.models

data class TripSummary(
    val startTime: Long,
    val endTime: Long,
    val totalDistanceKm: Double,
    val averageSpeedKmh: Double,
    val durationMinutes: Int,
    val safetyScore: Double
)