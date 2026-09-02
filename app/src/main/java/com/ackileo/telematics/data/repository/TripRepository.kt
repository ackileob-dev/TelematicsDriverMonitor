package com.ackileo.telematics.data.repository

import com.ackileo.telematics.data.local.dao.DriverDao
import com.ackileo.telematics.data.local.entities.TripEntity
import com.ackileo.telematics.data.remote.ApiService
import com.ackileo.telematics.data.remote.dto.TripDto
import com.ackileo.telematics.data.remote.models.EventRequest
import com.ackileo.telematics.data.remote.models.TripEndRequest
import com.ackileo.telematics.data.remote.models.TripResponse
import com.ackileo.telematics.data.remote.models.TripStartRequest
import com.ackileo.telematics.data.remote.models.TripSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository Interface defining Trip operations.
 * Handles the data strategy (Network + Local Cache).
 */
interface TripRepository {
    suspend fun startTrip(request: TripStartRequest): Result<TripResponse>
    suspend fun endTrip(request: TripEndRequest): Result<TripResponse>
    suspend fun sendEvent(request: EventRequest): Result<Unit>
    fun getTripHistory(): Flow<List<TripSummary>>
    suspend fun refreshTrips(): Result<Unit>
}

@Singleton
class TripRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val driverDao: DriverDao

) : TripRepository {

    /**
     * Start a trip via API.
     */
    override suspend fun startTrip(request: TripStartRequest): Result<TripResponse> =
        withContext(Dispatchers.IO) {
            runCatching { apiService.startTrip(request) }
        }

    /**
     * End a trip via API and immediately cache the result to Room.
     */
    override suspend fun endTrip(request: TripEndRequest): Result<TripResponse> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.endTrip(request)
                // Cache the finished trip details locally
                driverDao.insertTrip(response.toEntity())
                response
            }
        }

    /**
     * Send telematics events (harsh braking, speeding) to the server.
     */
    override suspend fun sendEvent(request: EventRequest): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.sendEvent(request)
                Unit
            }
        }

    /**
     * Provides a reactive stream of trips from the local database.
     */
    override fun getTripHistory(): Flow<List<TripSummary>> =
        driverDao.getRecentTrips()
            .distinctUntilChanged()
            .map { entities: List<TripEntity> ->
                entities.map { it.toDomainModel() }
            }

    /**
     * Synchronizes local storage with the remote API.
     *
     * Previously this called apiService.getTrips() with no arguments (compile error)
     * and assigned the result to List<TripResponse> (type mismatch).  Fixed to call
     * the correct overload with explicit nullable query parameters and map the
     * returned TripDto list through a TripDto→TripEntity mapper.
     */
    override suspend fun refreshTrips(): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val apiResponse = apiService.getTrips(
                    page = null,
                    limit = null,
                    driverId = null,
                    vehicleId = null,
                    status = null,
                )

                if (apiResponse.success == false) {
                    throw IllegalStateException(
                        apiResponse.message
                            ?: apiResponse.error?.message
                            ?: "Failed to refresh trips"
                    )
                }

                val entities = apiResponse.data.orEmpty().map { it.toEntity() }
                driverDao.clearAllTrips()
                if (entities.isNotEmpty()) {
                    driverDao.insertTrips(entities)
                }
                Unit
            }
        }
}

// ── Domain mappers ────────────────────────────────────────────────────────────

/**
 * Mapper: Database Entity → Domain Model (UI Model)
 */
private fun TripEntity.toDomainModel(): TripSummary = TripSummary(
    startTime = this.startTime,
    endTime = this.endTime,
    totalDistanceKm = this.totalDistanceKm,
    averageSpeedKmh = this.averageSpeedKmh,
    durationMinutes = this.durationMinutes,
    safetyScore = this.safetyScore
)

/**
 * Mapper: Legacy Network Response → Database Entity
 */
private fun TripResponse.toEntity(): TripEntity = TripEntity(
    startTime = this.startTime?.toLongOrNull() ?: 0L,
    endTime = this.endTime?.toLongOrNull() ?: 0L,
    totalDistanceKm = this.totalDistance ?: 0.0,
    averageSpeedKmh = this.averageSpeed ?: 0.0,
    durationMinutes = this.duration ?: 0,
    safetyScore = this.safetyScore ?: 0.0,
    destination = this.destination ?: "Unknown"
)

/**
 * Mapper: New DTO Network Response → Database Entity.
 * ISO-8601 timestamps (e.g. "2026-08-22T10:30:00.000Z") are parsed to epoch millis.
 */
private fun TripDto.toEntity(): TripEntity = TripEntity(
    startTime = parseIsoTimestamp(this.startTime),
    endTime = parseIsoTimestamp(this.endTime),
    totalDistanceKm = this.totalDistance ?: 0.0,
    averageSpeedKmh = this.averageSpeed ?: 0.0,
    durationMinutes = this.duration ?: 0,
    safetyScore = this.safetyScore ?: 0.0,
    destination = "Unknown"
)

/**
 * Parses an ISO-8601 timestamp string ("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" or without millis)
 * into epoch milliseconds.  Returns 0 on any parse failure so callers never crash.
 */
private fun parseIsoTimestamp(timestamp: String?): Long {
    if (timestamp.isNullOrBlank()) return 0L
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
    )
    for (pattern in patterns) {
        return try {
            SimpleDateFormat(pattern, Locale.US)
                .also { it.timeZone = TimeZone.getTimeZone("UTC") }
                .parse(timestamp)
                ?.time ?: continue
        } catch (_: Exception) {
            continue
        }
    }
    return 0L
}
