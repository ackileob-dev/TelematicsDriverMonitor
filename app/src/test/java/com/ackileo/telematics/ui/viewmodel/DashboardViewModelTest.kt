package com.ackileo.telematics.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ackileo.telematics.domain.model.usecase.CalculateSafetyScoreUseCase
import com.ackileo.telematics.domain.model.usecase.EndTripUseCase
import com.ackileo.telematics.domain.model.usecase.StartTripUseCase
import com.ackileo.telematics.utils.DrivingEvent
import com.ackileo.telematics.utils.DrivingSensorManager
import com.ackileo.telematics.utils.LocationData
import com.ackileo.telematics.utils.LocationTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val locationTracker: LocationTracker = mock()
    private val sensorManager: DrivingSensorManager = mock()
    private val calculateScore: CalculateSafetyScoreUseCase = mock()
    private val startTripUseCase: StartTripUseCase = mock()
    private val endTripUseCase: EndTripUseCase = mock()

    private lateinit var viewModel: DashboardViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        whenever(locationTracker.locationDetails).thenReturn(
            MutableStateFlow(
                LocationData(
                    latitude = 0.0,
                    longitude = 0.0,
                    speedKmh = 0f,
                    totalDistanceKm = 0.0
                )
            )
        )

        whenever(sensorManager.events).thenReturn(
            MutableStateFlow(DrivingEvent.IDLE)
        )

        whenever(calculateScore.invoke(0, 0, 0, 0)).thenReturn(100)

        viewModel = DashboardViewModel(
            locationTracker = locationTracker,
            sensorManager = sensorManager,
            calculateSafetyScoreUseCase = calculateScore,
            startTripUseCase = startTripUseCase,
            endTripUseCase = endTripUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testStartTripStartsTrackingAndActivatesTrip() = runTest {
        whenever(startTripUseCase.invoke()).thenReturn(1000L)

        viewModel.startTrip()

        advanceUntilIdle()

        verify(startTripUseCase).invoke()
        verify(locationTracker).startTracking()
        verify(sensorManager).startMonitoring()

        assertTrue(viewModel.uiState.value.isTripActive)
    }

    @Test
    fun testEndTripStopsTrackingAndDeactivatesTrip() = runTest {
        whenever(startTripUseCase.invoke()).thenReturn(1000L)

        viewModel.startTrip()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isTripActive)

        viewModel.endTrip()

        advanceUntilIdle()

        verify(locationTracker).stopTracking()
        verify(sensorManager).stopMonitoring()
        verify(endTripUseCase).invoke(1000L, 100)

        assertFalse(viewModel.uiState.value.isTripActive)
    }
}