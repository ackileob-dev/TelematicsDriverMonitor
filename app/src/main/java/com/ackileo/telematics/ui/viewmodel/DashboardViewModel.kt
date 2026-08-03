package com.ackileo.telematics.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ackileo.telematics.domain.model.usecase.CalculateSafetyScoreUseCase
import com.ackileo.telematics.domain.model.usecase.EndTripUseCase
import com.ackileo.telematics.domain.model.usecase.StartTripUseCase
import com.ackileo.telematics.utils.DrivingEvent
import com.ackileo.telematics.utils.DrivingSensorManager
import com.ackileo.telematics.utils.LocationData
import com.ackileo.telematics.utils.LocationTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class DashboardUiState(
    val currentSpeed: Float = 0f,
    val totalDistance: Double = 0.0,
    val safetyScore: Int = 100,
    val activeAlert: DrivingEvent = DrivingEvent.IDLE,
    val isGpsEnabled: Boolean = false,
    val isTripActive: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val locationTracker: LocationTracker,
    private val sensorManager: DrivingSensorManager,
    private val calculateSafetyScoreUseCase: CalculateSafetyScoreUseCase,
    private val startTripUseCase: StartTripUseCase,
    private val endTripUseCase: EndTripUseCase
) : ViewModel() {

    private var tripStartTime: Long = 0
    private val _isTripActive = MutableStateFlow(false)

    // Counters for Safety Score
    private val overSpeedCount = MutableStateFlow(0)
    private val harshBrakeCount = MutableStateFlow(0)
    private val rapidAccelCount = MutableStateFlow(0)
    private val phoneUsageCount = MutableStateFlow(0)

    /**
     * The Main UI State: Combines GPS, Sensors, and Internal Counters
     */
    val uiState: StateFlow<DashboardUiState> = combine(
        locationTracker.locationDetails,
        sensorManager.events,
        _isTripActive,
        overSpeedCount,
        harshBrakeCount,
        rapidAccelCount,
        phoneUsageCount
    ) { flows ->
        val location = flows[0] as LocationData
        val event = flows[1] as DrivingEvent
        val isActive = flows[2] as Boolean
        val over = flows[3] as Int
        val harsh = flows[4] as Int
        val rapid = flows[5] as Int
        val phone = flows[6] as Int

        DashboardUiState(
            currentSpeed = location.speedKmh,
            totalDistance = location.totalDistanceKm,
            safetyScore = calculateSafetyScoreUseCase(
                overSpeedCount = over,
                harshBrakeCount = harsh,
                rapidAccelCount = rapid,
                phoneUsageCount = phone
            ),
            activeAlert = event,
            isGpsEnabled = location.latitude != 0.0,
            isTripActive = isActive
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )

    init {
        observeDrivingEvents()
    }

    private fun observeDrivingEvents() {
        // Observe behavioral events (Braking, Accel, Phone)
        sensorManager.events
            .filter { _isTripActive.value } // Only count if trip is active
            .onEach { event ->
                when (event) {
                    DrivingEvent.HARSH_BRAKING -> harshBrakeCount.value++
                    DrivingEvent.RAPID_ACCELERATION -> rapidAccelCount.value++
                    DrivingEvent.PHONE_USAGE -> phoneUsageCount.value++
                    else -> {}
                }
            }
            .launchIn(viewModelScope)

        // Observe speed for over speeding
        locationTracker.locationDetails
            .map { it.speedKmh }
            .distinctUntilChanged()
            .filter { _isTripActive.value && it > 100f }
            .onEach { overSpeedCount.value++ }
            .launchIn(viewModelScope)
    }

    /**
     * Called by DashboardScreen.kt to Start Trip
     */
    fun startTrip() {
        tripStartTime = startTripUseCase()
        _isTripActive.value = true
        startTracking()
    }

    /**
     * Called by DashboardScreen.kt to End Trip
     */
    fun endTrip() {
        if (!_isTripActive.value) return

        // Final score calculation for the summary
        val finalScore = uiState.value.safetyScore
        endTripUseCase(tripStartTime, finalScore)

        _isTripActive.value = false
        stopTracking()
        resetCounters()
    }

    private fun startTracking() {
        locationTracker.startTracking()
        sensorManager.startMonitoring()
    }

    private fun stopTracking() {
        locationTracker.stopTracking()
        sensorManager.stopMonitoring()
    }

    private fun resetCounters() {
        overSpeedCount.value = 0
        harshBrakeCount.value = 0
        rapidAccelCount.value = 0
        phoneUsageCount.value = 0
    }
}