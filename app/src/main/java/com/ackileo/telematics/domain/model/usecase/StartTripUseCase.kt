package com.ackileo.telematics.domain.model.usecase



import com.ackileo.telematics.utils.DrivingSensorManagerPort
import com.ackileo.telematics.utils.LocationTrackerPort
import javax.inject.Inject

class StartTripUseCase @Inject constructor(
    private val locationTracker: LocationTrackerPort,
    private val sensorManager: DrivingSensorManagerPort
) {
    /**
     * Starts the GPS and Sensor tracking and returns the current timestamp.
     */
    operator fun invoke(): Long {
        locationTracker.startTracking()
        sensorManager.startMonitoring()
        return System.currentTimeMillis()
    }
}