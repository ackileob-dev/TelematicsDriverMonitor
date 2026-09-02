package com.ackileo.telematics.data.repository

import com.ackileo.telematics.data.remote.ApiService
import com.ackileo.telematics.data.remote.ApiResponse
import com.ackileo.telematics.data.local.TokenManager
import com.ackileo.telematics.data.remote.dto.AlertDto
import com.ackileo.telematics.data.remote.dto.DriverDto
import com.ackileo.telematics.data.remote.dto.DrivingEventDto
import com.ackileo.telematics.data.remote.dto.RewardDto
import com.ackileo.telematics.data.remote.dto.SafetyScoreDto
import com.ackileo.telematics.data.remote.dto.TripDto
import com.ackileo.telematics.data.remote.dto.VehicleDto
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.CancellationException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers


data class DashboardData(
    val driver: DriverDto,
    val safetyScore: SafetyScoreDto?,
    val recentTrips: List<TripDto>,
    val recentDrivingEvents: List<DrivingEventDto>,
    val rewards: List<RewardDto>,
    val alerts: List<AlertDto>,
    val vehicle: VehicleDto?,
)

interface DashboardRepository {
    suspend fun loadDashboard(): Result<DashboardData>
}

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
) : DashboardRepository {

    override suspend fun loadDashboard(): Result<DashboardData> = withContext(Dispatchers.IO) {
        try {
            coroutineScope {
                val userDeferred = async { apiService.getCurrentUser() }

                val userResponse = userDeferred.await()
                if (!userResponse.isSuccessful) {
                    return@coroutineScope Result.failure(
                        DashboardBackendException(
                            userResponse.code(),
                            userResponse.body().errorMessage("Unable to load dashboard profile")
                        )
                    )
                }

                val userBody = userResponse.body()
                val driver = userBody?.data ?: return@coroutineScope Result.failure(
                    DashboardBackendException(userResponse.code(), userBody?.message ?: "Dashboard profile is empty")
                )

                val driverId = driver.id

                val safetyDeferred = async { apiService.getSafetyScores(page = 1, limit = 1, driverId = driverId) }
                val tripsDeferred = async { apiService.getTrips(page = 1, limit = 5, driverId = driverId, vehicleId = null, status = null) }
                val eventsDeferred = async { apiService.getDrivingEvents(tripId = null, type = null, page = 1, limit = 5) }
                val rewardsDeferred = async { apiService.getRewards(page = 1, limit = 5, driverId = driverId) }
                val alertsDeferred = async { apiService.getAlerts(driverId = driverId, level = null, page = 1, limit = 5) }
                val vehiclesDeferred = async { apiService.getVehicles(page = 1, limit = 1, driverId = driverId) }

                val safetyResponse = safetyDeferred.await().requireSuccess("Unable to load safety score")
                val tripsResponse = tripsDeferred.await().requireSuccess("Unable to load trip history")
                val eventsResponse = eventsDeferred.await().requireSuccess("Unable to load driving events")
                val rewardsResponse = rewardsDeferred.await().requireSuccess("Unable to load rewards")
                val alertsResponse = alertsDeferred.await().requireSuccess("Unable to load alerts")
                val vehiclesResponse = vehiclesDeferred.await().requireSuccess("Unable to load vehicle data")

                val safetyScore = safetyResponse.data?.firstOrNull()
                val recentTrips = tripsResponse.data.orEmpty()
                val recentDrivingEvents = eventsResponse.data.orEmpty()
                val rewards = rewardsResponse.data.orEmpty()
                val alerts = alertsResponse.data.orEmpty()
                val vehicle = vehiclesResponse.data?.firstOrNull()

                Result.success(
                    DashboardData(
                        driver = driver,
                        safetyScore = safetyScore,
                        recentTrips = recentTrips,
                        recentDrivingEvents = recentDrivingEvents,
                        rewards = rewards,
                        alerts = alerts,
                        vehicle = vehicle,
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Result.failure(DashboardBackendException(null, "Network error. Check your connection and retry.", e))
        } catch (e: HttpException) {
            if (e.code() == 401) {
                tokenManager.clearAuthState()
            }
            Result.failure(DashboardBackendException(e.code(), mapHttpMessage(e.code(), e.message())))
        } catch (e: Exception) {
            Result.failure(DashboardBackendException(null, e.message ?: "Failed to load dashboard", e))
        }
    }

    private fun <T> ApiResponse<T>.requireSuccess(fallback: String): ApiResponse<T> {
        if (success == false) {
            throw DashboardBackendException(null, errorMessage(fallback))
        }
        return this
    }

    private fun mapHttpMessage(code: Int, fallback: String): String {
        return when (code) {
            400 -> "Invalid request while loading dashboard data."
            401 -> "Session expired. Please sign in again."
            409 -> "Dashboard data conflict. Please refresh and try again."
            500 -> "Server error while loading dashboard. Please try again later."
            else -> fallback.ifBlank { "Unable to load dashboard" }
        }
    }

    private fun <T> ApiResponse<T>?.errorMessage(fallback: String): String {
        return this?.message?.takeIf { it.isNotBlank() }
            ?: this?.error?.message?.takeIf { it.isNotBlank() }
            ?: fallback
    }
}

private class DashboardBackendException(
    val code: Int?,
    override val message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
