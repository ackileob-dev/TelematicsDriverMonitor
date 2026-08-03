

package com.ackileo.telematics.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val speedKmh: Float = 0f,
    val totalDistanceKm: Double = 0.0
)

@Singleton
class LocationTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val _locationDetails = MutableStateFlow(LocationData())
    val locationDetails = _locationDetails.asStateFlow()

    private var lastLocation: Location? = null
    private var totalDistanceMeters = 0.0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                calculateMetrics(location)
            }
        }
    }

    private fun calculateMetrics(currentLocation: Location) {
        // Calculate Distance
        lastLocation?.let { last ->
            val distance = last.distanceTo(currentLocation)
            if (currentLocation.accuracy < 20) { // Only count if accuracy is decent
                totalDistanceMeters += distance
            }
        }

        // Calculate Speed (m/s to km/h)
        val speedKmh = (currentLocation.speed * 3.6f)

        _locationDetails.value = LocationData(
            latitude = currentLocation.latitude,
            longitude = currentLocation.longitude,
            speedKmh = speedKmh,
            totalDistanceKm = totalDistanceMeters / 1000.0
        )

        lastLocation = currentLocation
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setMinUpdateIntervalMillis(1000)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}