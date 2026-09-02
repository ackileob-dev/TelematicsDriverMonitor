package com.ackileo.telematics.domain.model.usecase



import com.ackileo.telematics.data.remote.models.TripSummary
import com.ackileo.telematics.utils.DrivingSensorManagerPort
import com.ackileo.telematics.utils.LocationTrackerPort
import javax.inject.Inject

class EndTripUseCase @Inject constructor(
    private val locationTracker: LocationTrackerPort,
    private val sensorManager: DrivingSensorManagerPort,
    private val calculateSafetyScoreUseCase: CalculateSafetyScoreUseCase
) {
    /**
     * Ends the trip and calculates final metrics.
     * @param startTime The timestamp when the trip started.
     * @param currentSafetyScore The score accumulated during the trip.
     */
    operator fun invoke(startTime: Long, currentSafetyScore: Int): TripSummary {
        val endTime = System.currentTimeMillis()
        val locationData = locationTracker.locationDetails.value

        locationTracker.stopTracking()
        sensorManager.stopMonitoring()

        val durationMillis = endTime - startTime

        // Fix: Convert the result to Int to match TripSummary expectations
        val durationMinutes = (durationMillis / (1000 * 60)).toInt()

        val durationHours = durationMillis / (1000.0 * 60 * 60)
        val avgSpeed = if (durationHours > 0) {
            locationData.totalDistanceKm / durationHours
        } else {
            0.0
        }

        return TripSummary(
            startTime = startTime,
            endTime = endTime,
            totalDistanceKm = locationData.totalDistanceKm,
            averageSpeedKmh = avgSpeed,
            durationMinutes = durationMinutes, // Now matches expected Int type
            safetyScore = currentSafetyScore.toDouble()
        )
    }
}