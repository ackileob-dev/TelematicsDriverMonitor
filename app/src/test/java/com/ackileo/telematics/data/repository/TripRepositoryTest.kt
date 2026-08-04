package com.ackileo.telematics.data.repository

import com.ackileo.telematics.data.local.dao.DriverDao
import com.ackileo.telematics.data.remote.ApiService
import com.ackileo.telematics.data.remote.models.TripResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

import org.mockito.kotlin.*


// Remove these if they are causing conflicts:
// import com.ackileo.telematics.domain.model.Trip.*
// import com.ackileo.telematics.data.remote.models.TripSummary.*

/**
 * Unit tests for [TripRepositoryImpl]
 */
class TripRepositoryTest {

    // 1. Mocks
    private val apiService: ApiService = mock()
    private val driverDao: DriverDao = mock()

    // 2. Test subject
    private lateinit var repository: TripRepositoryImpl

    @Before
    fun setup() {
        repository = TripRepositoryImpl(apiService, driverDao)
    }

    @Test
    fun testRefreshTripsSuccessClearsLocalCacheThenInsertsNewData() = runTest {
        // Arrange
        val mockTrips = listOf(createMockTrip(id = "trip_1"))
        whenever(apiService.getTrips()).thenReturn(mockTrips)

        // Act
        val result = repository.refreshTrips()

        // Assert
        // We use inOrder to verify that clearing occurs strictly BEFORE inserting
        val inOrder = inOrder(driverDao)
        inOrder.verify(driverDao).clearAllTrips()
        inOrder.verify(driverDao).insertTrips(any())

        assertTrue("Repository should return success on valid API response", result.isSuccess)
    }

    @Test
    fun testRefreshTripsEmptyListClearsCacheAndCompletesSuccessfully() = runTest {
        // Arrange
        whenever(apiService.getTrips()).thenReturn(emptyList())

        // Act
        val result = repository.refreshTrips()

        // Assert
        verify(driverDao).clearAllTrips()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testRefreshTripsNetworkFailureReturnsFailureAndPreservesLocalCache() = runTest {
        // Arrange
        val networkError = RuntimeException("Network Error")
        whenever(apiService.getTrips()).thenThrow(networkError)

        // Act
        val result = repository.refreshTrips()

        // Assert
        assertTrue("Repository should return failure on exception", result.isFailure)

        // Ensure database was not touched if the network fetch failed
        verify(driverDao, never()).clearAllTrips()
        verify(driverDao, never()).insertTrips(any())
    }

    /**
     * Helper Factory: Updated to match the String-based date format
     * and the field names in the refactored TripResponse model.
     */
    private fun createMockTrip(
        id: String = "trip_001",
        score: Double = 90.0 // Changed to Double to match Safety Score logic
    ): TripResponse {
        return TripResponse(
            // Ensure these names match your actual TripResponse class exactly
            id = id,
            startTime = "2023-10-27T10:00:00Z", // Fixed: Changed Long to String
            endTime = "2023-10-27T10:30:00Z",   // Fixed: Changed Long to String
            totalDistance = 15.5,
            averageSpeed = 45.0,
            duration = 30,
            safetyScore = score

        )
    }
}