package com.ackileo.telematics.domain.model.usecase



import com.ackileo.telematics.utils.DrivingSensorManager
import com.ackileo.telematics.utils.LocationTracker
import javax.inject.Inject

class StartTripUseCase @Inject constructor(
    private val locationTracker: LocationTracker,
    private val sensorManager: DrivingSensorManager
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