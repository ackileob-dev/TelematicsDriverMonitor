package com.ackileo.telematics.data.repository

import com.ackileo.telematics.data.local.dao.DriverDao
import com.ackileo.telematics.data.remote.ApiResponse
import com.ackileo.telematics.data.remote.ApiService
import com.ackileo.telematics.data.remote.dto.TripDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

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
        // Arrange – stub the 5-parameter overload that refreshTrips() now calls.
        val mockTrips = listOf(createMockTripDto(id = "trip_1"))
        val apiResponse = ApiResponse(success = true, data = mockTrips)
        whenever(
            apiService.getTrips(
                page = null,
                limit = null,
                driverId = null,
                vehicleId = null,
                status = null,
            )
        ).thenReturn(apiResponse)

        // Act
        val result = repository.refreshTrips()

        // Assert – clear must happen strictly before insert (atomic refresh)
        val inOrder = inOrder(driverDao)
        inOrder.verify(driverDao).clearAllTrips()
        inOrder.verify(driverDao).insertTrips(any())

        assertTrue("Repository should return success on valid API response", result.isSuccess)
    }

    @Test
    fun testRefreshTripsEmptyListClearsCacheAndCompletesSuccessfully() = runTest {
        // Arrange
        val apiResponse = ApiResponse<List<TripDto>>(success = true, data = emptyList())
        whenever(
            apiService.getTrips(
                page = null,
                limit = null,
                driverId = null,
                vehicleId = null,
                status = null,
            )
        ).thenReturn(apiResponse)

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
        whenever(
            apiService.getTrips(
                page = null,
                limit = null,
                driverId = null,
                vehicleId = null,
                status = null,
            )
        ).thenThrow(networkError)

        // Act
        val result = repository.refreshTrips()

        // Assert
        assertTrue("Repository should return failure on exception", result.isFailure)

        // Ensure database was not touched if the network fetch failed
        verify(driverDao, never()).clearAllTrips()
        verify(driverDao, never()).insertTrips(any())
    }

    /**
     * Helper Factory: produces a [TripDto] with the fields that refreshTrips() maps
     * to [com.ackileo.telematics.data.local.entities.TripEntity].
     */
    private fun createMockTripDto(
        id: String = "trip_001",
        score: Double = 90.0,
    ): TripDto = TripDto(
        id = id,
        startTime = "2023-10-27T10:00:00.000Z",
        endTime = "2023-10-27T10:30:00.000Z",
        totalDistance = 15.5,
        averageSpeed = 45.0,
        duration = 30,
        safetyScore = score,
    )
}