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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // 1. Mocks
    private val locationTracker: LocationTracker = mock()
    private val sensorManager: DrivingSensorManager = mock()
    private val calculateScore: CalculateSafetyScoreUseCase = mock()
    private val startTrip: StartTripUseCase = mock()
    private val endTrip: EndTripUseCase = mock()

    private lateinit var viewModel: DashboardViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mocking mandatory StateFlows/Flows collected in ViewModel init {}
        // Use MutableStateFlow because a standard Flow cannot be cast to StateFlow
        whenever(locationTracker.locationDetails).thenReturn(
            MutableStateFlow(LocationData(0.0, 0.0, 0f, 0.0))
        )

        whenever(sensorManager.events).thenReturn(
            flowOf(DrivingEvent.IDLE) as StateFlow<DrivingEvent>?
        )

        viewModel = DashboardViewModel(
            locationTracker,
            sensorManager,
            calculateScore,
            startTrip,
            endTrip
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onStartTrip triggers tracking and updates state`() = runTest {
        // Arrange
        val mockTripId = 1000L
        whenever(startTrip.invoke()).thenReturn(mockTripId)

        // Act
        viewModel.onStartTrip()
        advanceUntilIdle() // Process the coroutine inside the ViewModel

        // Assert
        verify(locationTracker).startTracking()
        verify(sensorManager).startMonitoring()
        assertTrue("UI State should show active trip", viewModel.uiState.value.isTripActive)
    }

    @Test
    fun `onEndTrip stops tracking and resets state`() = runTest {
        // Arrange: Start a trip first
        whenever(startTrip.invoke()).thenReturn(1000L)
        viewModel.onStartTrip()
        advanceUntilIdle()

        // Sanity check
        assertTrue(viewModel.uiState.value.isTripActive)

        // Act
        viewModel.onEndTrip()
        advanceUntilIdle()

        // Assert
        verify(locationTracker).stopTracking()
        verify(sensorManager).stopMonitoring()
        assertFalse("UI State should show inactive trip", viewModel.uiState.value.isTripActive)
    }
}