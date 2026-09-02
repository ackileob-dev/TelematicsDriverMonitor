package com.ackileo.telematics.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ackileo.telematics.data.remote.dto.DriverDto
import com.ackileo.telematics.data.repository.DashboardData
import com.ackileo.telematics.data.repository.DashboardRepository
import com.ackileo.telematics.domain.model.usecase.CalculateSafetyScoreUseCase
import com.ackileo.telematics.domain.model.usecase.EndTripUseCase
import com.ackileo.telematics.domain.model.usecase.StartTripUseCase
import com.ackileo.telematics.test.MainDispatcherRule
import com.ackileo.telematics.utils.DrivingEvent
import com.ackileo.telematics.utils.DrivingSensorManagerPort
import com.ackileo.telematics.utils.LocationData
import com.ackileo.telematics.utils.LocationTrackerPort
import com.ackileo.telematics.utils.SensorTelemetry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeDashboardRepository : DashboardRepository {
        var result: Result<DashboardData> = Result.failure(IllegalStateException("Dashboard result not configured"))
        override suspend fun loadDashboard(): Result<DashboardData> = result
    }

    private class FakeLocationTracker : LocationTrackerPort {
        override val locationDetails = MutableStateFlow(LocationData())
        var startTrackingCalls = 0
        var stopTrackingCalls = 0

        override fun startTracking() {
            startTrackingCalls++
        }

        override fun stopTracking() {
            stopTrackingCalls++
        }
    }

    private class FakeSensorManager : DrivingSensorManagerPort {
        override val events = MutableStateFlow(DrivingEvent.IDLE)
        override val telemetry = MutableStateFlow(SensorTelemetry())
        var startMonitoringCalls = 0
        var stopMonitoringCalls = 0

        override fun startMonitoring() {
            startMonitoringCalls++
        }

        override fun stopMonitoring() {
            stopMonitoringCalls++
        }
    }

    private val dashboardRepository = FakeDashboardRepository()
    private val locationTracker = FakeLocationTracker()
    private val sensorManager = FakeSensorManager()
    private val calculateScore = CalculateSafetyScoreUseCase()

    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        val minimalDashboardData = DashboardData(
            driver = DriverDto(id = "test-driver", fullName = "Test Driver", email = null, phone = null),
            safetyScore = null,
            recentTrips = emptyList(),
            recentDrivingEvents = emptyList(),
            rewards = emptyList(),
            alerts = emptyList(),
            vehicle = null,
        )
        dashboardRepository.result = Result.success(minimalDashboardData)

        locationTracker.locationDetails.value = LocationData(
            latitude = 0.0,
            longitude = 0.0,
            speedKmh = 0f,
            totalDistanceKm = 0.0,
            accuracyMeters = Float.MAX_VALUE,
            timestampMillis = 0L,
        )
        sensorManager.events.value = DrivingEvent.IDLE
        sensorManager.telemetry.value = SensorTelemetry()

        val startTripUseCase = StartTripUseCase(locationTracker, sensorManager)
        val endTripUseCase = EndTripUseCase(locationTracker, sensorManager, calculateScore)

        viewModel = DashboardViewModel(
            dashboardRepository = dashboardRepository,
            locationTracker = locationTracker,
            sensorManager = sensorManager,
            calculateSafetyScoreUseCase = calculateScore,
            startTripUseCase = startTripUseCase,
            endTripUseCase = endTripUseCase,
        )
    }

    @Test
    fun testStartTripStartsTrackingAndActivatesTrip() = runTest {
        val collector = launch { viewModel.uiState.collect { } }

        viewModel.startTrip()
        advanceUntilIdle()

        assertTrue(locationTracker.startTrackingCalls >= 1)
        assertTrue(sensorManager.startMonitoringCalls >= 1)
        assertTrue(viewModel.uiState.value.isTripActive)

        collector.cancel()
    }

    @Test
    fun testEndTripStopsTrackingAndDeactivatesTrip() = runTest {
        val collector = launch { viewModel.uiState.collect { } }

        viewModel.startTrip()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isTripActive)

        viewModel.endTrip()
        advanceUntilIdle()

        assertTrue(locationTracker.stopTrackingCalls >= 1)
        assertTrue(sensorManager.stopMonitoringCalls >= 1)
        assertFalse(viewModel.uiState.value.isTripActive)

        collector.cancel()
    }
}