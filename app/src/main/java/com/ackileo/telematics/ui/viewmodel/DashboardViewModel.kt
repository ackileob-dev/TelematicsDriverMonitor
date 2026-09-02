package com.ackileo.telematics.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ackileo.telematics.data.repository.DashboardData
import com.ackileo.telematics.data.repository.DashboardRepository
import com.ackileo.telematics.domain.model.usecase.CalculateSafetyScoreUseCase
import com.ackileo.telematics.domain.model.usecase.EndTripUseCase
import com.ackileo.telematics.domain.model.usecase.StartTripUseCase
import com.ackileo.telematics.utils.DrivingEvent
import com.ackileo.telematics.utils.DrivingSensorManagerPort
import com.ackileo.telematics.utils.LocationTrackerPort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val currentSpeed: Float = 0f,
    val totalDistance: Double = 0.0,
    val safetyScore: Int = 100,
    val activeAlert: DrivingEvent = DrivingEvent.IDLE,
    val isGpsEnabled: Boolean = false,
    val isTripActive: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmpty: Boolean = false,
    val dashboardData: DashboardData? = null,
)

private data class DashboardBackendState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmpty: Boolean = false,
    val data: DashboardData? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val locationTracker: LocationTrackerPort,
    private val sensorManager: DrivingSensorManagerPort,
    private val calculateSafetyScoreUseCase: CalculateSafetyScoreUseCase,
    private val startTripUseCase: StartTripUseCase,
    private val endTripUseCase: EndTripUseCase,
) : ViewModel() {

    private var tripStartTime: Long = 0
    private val _isTripActive = MutableStateFlow(false)
    private val _backendState = MutableStateFlow(DashboardBackendState(isLoading = true))

    private val overSpeedCount = MutableStateFlow(0)
    private val harshBrakeCount = MutableStateFlow(0)
    private val rapidAccelCount = MutableStateFlow(0)
    private val phoneUsageCount = MutableStateFlow(0)

    private val liveUiState: StateFlow<DashboardUiState> = combine(
        locationTracker.locationDetails,
        sensorManager.events,
        _isTripActive,
        overSpeedCount,
        harshBrakeCount,
        rapidAccelCount,
        phoneUsageCount,
    ) { flows ->
        val location = flows[0] as com.ackileo.telematics.utils.LocationData
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
                phoneUsageCount = phone,
            ),
            activeAlert = event,
            isGpsEnabled = location.latitude != 0.0,
            isTripActive = isActive,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(),
    )

    val uiState: StateFlow<DashboardUiState> = combine(liveUiState, _backendState.asStateFlow()) { live, backend ->
        val data = backend.data
        live.copy(
            safetyScore = data?.safetyScore?.score ?: live.safetyScore,
            isLoading = backend.isLoading,
            errorMessage = backend.errorMessage,
            isEmpty = backend.isEmpty,
            dashboardData = data,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = liveUiState.value,
    )

    init {
        observeDrivingEvents()
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _backendState.value = DashboardBackendState(isLoading = true)
            dashboardRepository.loadDashboard().fold(
                onSuccess = { data ->
                    val isEmpty = data.recentTrips.isEmpty() &&
                        data.recentDrivingEvents.isEmpty() &&
                        data.rewards.isEmpty() &&
                        data.alerts.isEmpty() &&
                        data.vehicle == null &&
                        data.safetyScore == null

                    _backendState.value = DashboardBackendState(
                        isLoading = false,
                        errorMessage = null,
                        isEmpty = isEmpty,
                        data = data,
                    )
                },
                onFailure = { error ->
                    _backendState.value = DashboardBackendState(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load dashboard",
                        isEmpty = false,
                        data = null,
                    )
                },
            )
        }
    }

    fun retryDashboardLoad() {
        loadDashboard()
    }

    private fun observeDrivingEvents() {
        sensorManager.events
            .filter { _isTripActive.value }
            .onEach { event ->
                when (event) {
                    DrivingEvent.HARSH_BRAKING -> harshBrakeCount.value++
                    DrivingEvent.RAPID_ACCELERATION -> rapidAccelCount.value++
                    DrivingEvent.PHONE_USAGE -> phoneUsageCount.value++
                    else -> Unit
                }
            }
            .launchIn(viewModelScope)

        locationTracker.locationDetails
            .map { it.speedKmh }
            .distinctUntilChanged()
            .filter { _isTripActive.value && it > 100f }
            .onEach { overSpeedCount.value++ }
            .launchIn(viewModelScope)
    }

    fun startTrip() {
        tripStartTime = startTripUseCase()
        _isTripActive.value = true
        startTracking()
    }

    fun endTrip() {
        if (!_isTripActive.value) return

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