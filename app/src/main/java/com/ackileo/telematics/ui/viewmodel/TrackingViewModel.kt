package com.ackileo.telematics.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ackileo.telematics.data.repository.TrackingEventRecord
import com.ackileo.telematics.data.repository.TrackingRepository
import com.ackileo.telematics.data.repository.TripTrackingSession
import com.ackileo.telematics.utils.DrivingEvent
import com.ackileo.telematics.utils.DrivingSensorManager
import com.ackileo.telematics.utils.LocationData
import com.ackileo.telematics.utils.LocationTracker
import com.ackileo.telematics.utils.SensorTelemetry
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrackingUiState(
    val isTracking: Boolean = false,
    val hasLocationPermission: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentTripId: String? = null,
    val location: LocationData = LocationData(),
    val sensorTelemetry: SensorTelemetry = SensorTelemetry(),
    val activeEvent: DrivingEvent = DrivingEvent.IDLE,
    val tripDurationSeconds: Long = 0L,
    val maxSpeedKmh: Float = 0f,
    val recentEvents: List<TrackingEventRecord> = emptyList(),
)

@HiltViewModel
class TrackingViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val locationTracker: LocationTracker,
    private val sensorManager: DrivingSensorManager,
    private val trackingRepository: TrackingRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TrackingUiState(hasLocationPermission = hasLocationPermission())
    )
    val uiState: StateFlow<TrackingUiState> = _uiState

    private var activeSession: TripTrackingSession? = null
    private var harshBrakeCount: Int = 0
    private var harshAccelerationCount: Int = 0
    private var phoneDistractionCount: Int = 0
    private var sharpCorneringCount: Int = 0

    init {
        combine(
            locationTracker.locationDetails,
            sensorManager.telemetry,
            sensorManager.events,
        ) { location, telemetry, event ->
            Triple(location, telemetry, event)
        }
            .onEach { (location, telemetry, event) ->
                val now = System.currentTimeMillis()
                val session = activeSession
                val durationSeconds = if (session != null && _uiState.value.isTracking) {
                    ((now - session.startedAtMillis).coerceAtLeast(0L) / 1000L)
                } else {
                    0L
                }
                val maxSpeed = if (_uiState.value.isTracking) {
                    maxOf(_uiState.value.maxSpeedKmh, location.speedKmh)
                } else {
                    _uiState.value.maxSpeedKmh
                }

                _uiState.value = _uiState.value.copy(
                    location = location,
                    sensorTelemetry = telemetry,
                    activeEvent = event,
                    tripDurationSeconds = durationSeconds,
                    maxSpeedKmh = maxSpeed,
                )
            }
            .launchIn(viewModelScope)

        combine(
            locationTracker.locationDetails,
            sensorManager.telemetry,
            sensorManager.events,
        ) { location, telemetry, event ->
            Triple(location, telemetry, event)
        }
            .filter { _uiState.value.isTracking && activeSession != null }
            .onEach { (location, telemetry, event) ->
                postEventIfNeeded(location, telemetry, event)
            }
            .launchIn(viewModelScope)
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _uiState.value = _uiState.value.copy(hasLocationPermission = granted)
    }

    fun toggleTracking() {
        if (_uiState.value.isTracking) {
            stopTracking()
        } else {
            startTracking()
        }
    }

    private fun startTracking() {
        if (!hasLocationPermission()) {
            _uiState.value = _uiState.value.copy(
                hasLocationPermission = false,
                errorMessage = "Location permission is required to start tracking."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            trackingRepository.startTripSession(_uiState.value.location).fold(
                onSuccess = { session ->
                    activeSession = session
                    locationTracker.startTracking()
                    sensorManager.startMonitoring()
                    resetEventCounters()
                    _uiState.value = _uiState.value.copy(
                        isTracking = true,
                        isLoading = false,
                        hasLocationPermission = true,
                        errorMessage = null,
                        currentTripId = session.tripId,
                        tripDurationSeconds = 0L,
                        maxSpeedKmh = 0f,
                    )
                },
                onFailure = { error ->
                    activeSession = null
                    _uiState.value = _uiState.value.copy(
                        isTracking = false,
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to start trip",
                    )
                },
            )
        }
    }

    private fun stopTracking() {
        viewModelScope.launch {
            val session = activeSession
            locationTracker.stopTracking()
            sensorManager.stopMonitoring()

            _uiState.value = _uiState.value.copy(isLoading = true)

            var completionError: String? = null
            if (session != null) {
                trackingRepository.endTripSession(
                    session = session,
                    locationData = _uiState.value.location,
                    maxSpeedKmh = _uiState.value.maxSpeedKmh,
                    safetyScore = estimateSafetyScore(),
                ).onFailure { error ->
                    completionError = error.message ?: "Failed to sync completed trip"
                }
            }

            activeSession = null
            _uiState.value = _uiState.value.copy(
                isTracking = false,
                isLoading = false,
                currentTripId = null,
                errorMessage = completionError,
            )
            resetEventCounters()
        }
    }

    private suspend fun postEventIfNeeded(
        locationData: LocationData,
        sensorTelemetry: SensorTelemetry,
        event: DrivingEvent,
    ) {
        val session = activeSession ?: return

        trackingRepository.processTelemetry(
            session = session,
            locationData = locationData,
            sensorTelemetry = sensorTelemetry,
            event = event,
        ).fold(
            onSuccess = { record ->
                if (record != null) {
                    when (record.eventType) {
                        "harsh_braking" -> harshBrakeCount++
                        "harsh_acceleration" -> harshAccelerationCount++
                        "phone_distraction" -> phoneDistractionCount++
                        "sharp_cornering" -> sharpCorneringCount++
                    }

                    _uiState.value = _uiState.value.copy(
                        recentEvents = (listOf(record) + _uiState.value.recentEvents).distinctBy {
                            "${it.eventType}-${it.timestampIso}"
                        }.take(20)
                    )
                }
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Failed to upload event",
                )
            },
        )
    }

    private fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    private fun estimateSafetyScore(): Double {
        val penalty = (harshBrakeCount * 4) +
            (harshAccelerationCount * 4) +
            (sharpCorneringCount * 3) +
            (phoneDistractionCount * 6)

        return (100 - penalty).coerceAtLeast(0).toDouble()
    }

    private fun resetEventCounters() {
        harshBrakeCount = 0
        harshAccelerationCount = 0
        phoneDistractionCount = 0
        sharpCorneringCount = 0
    }
}

