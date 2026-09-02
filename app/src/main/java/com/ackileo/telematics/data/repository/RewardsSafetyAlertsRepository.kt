package com.ackileo.telematics.data.repository

import com.ackileo.telematics.data.local.TokenManager
import com.ackileo.telematics.data.remote.ApiService
import com.ackileo.telematics.data.remote.dto.AlertDto
import com.ackileo.telematics.data.remote.dto.RewardDto
import com.ackileo.telematics.data.remote.dto.SafetyScoreDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

data class RewardsSafetyAlertsData(
    val rewards: List<RewardDto>,
    val safetyScoreHistory: List<SafetyScoreDto>,
    val latestSafetyScore: SafetyScoreDto?,
    val alerts: List<AlertDto>,
)

interface RewardsSafetyAlertsRepository {
    suspend fun loadAll(): Result<RewardsSafetyAlertsData>
    suspend fun markAlertRead(alert: AlertDto): Result<AlertDto>
}

@Singleton
class RewardsSafetyAlertsRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
) : RewardsSafetyAlertsRepository {

    override suspend fun loadAll(): Result<RewardsSafetyAlertsData> = withContext(Dispatchers.IO) {
        try {
            coroutineScope {
                val driverId = tokenManager.getDriverId()

                val rewardsDeferred = async { apiService.getRewards(page = 1, limit = 100, driverId = driverId) }
                val safetyScoresDeferred = async { apiService.getSafetyScores(driverId = driverId, page = 1, limit = 30) }
                val alertsDeferred = async { apiService.getAlerts(driverId = driverId, level = null, page = 1, limit = 50) }

                val rewardsResponse = rewardsDeferred.await().requireSuccess("Failed to load rewards")
                val safetyScoresResponse = safetyScoresDeferred.await().requireSuccess("Failed to load safety score history")
                val alertsResponse = alertsDeferred.await().requireSuccess("Failed to load alerts")

                val rewards = rewardsResponse.data.orEmpty()
                val safetyHistory = safetyScoresResponse.data.orEmpty()
                val latest = safetyHistory.maxByOrNull { it.computedAt.orEmpty() }
                val alerts = alertsResponse.data.orEmpty()

                Result.success(
                    RewardsSafetyAlertsData(
                        rewards = rewards,
                        safetyScoreHistory = safetyHistory,
                        latestSafetyScore = latest,
                        alerts = alerts,
                    )
                )
            }
        } catch (e: HttpException) {
            Result.failure(mapHttpException(e))
        } catch (e: IOException) {
            Result.failure(FeatureLoadException("Network error. Please check your internet connection and retry."))
        } catch (e: Exception) {
            Result.failure(FeatureLoadException(e.message ?: "Failed to load rewards, safety scores, and alerts."))
        }
    }

    override suspend fun markAlertRead(alert: AlertDto): Result<AlertDto> = withContext(Dispatchers.IO) {
        try {
            val updated = alert.copy(isRead = true, read = true)
            val response = apiService.updateAlert(alert.id, updated).requireSuccess("Failed to update alert status")
            Result.success(response.data ?: updated)
        } catch (e: HttpException) {
            Result.failure(mapHttpException(e))
        } catch (e: IOException) {
            Result.failure(FeatureLoadException("Network error. Please check your internet connection and retry."))
        } catch (e: Exception) {
            Result.failure(FeatureLoadException(e.message ?: "Failed to update alert status."))
        }
    }

    private fun <T> com.ackileo.telematics.data.remote.ApiResponse<T>.requireSuccess(
        fallback: String,
    ): com.ackileo.telematics.data.remote.ApiResponse<T> {
        if (success == false) {
            throw FeatureLoadException(message?.takeIf { it.isNotBlank() } ?: error?.message?.takeIf { it.isNotBlank() } ?: fallback)
        }
        return this
    }

    private fun mapHttpException(exception: HttpException): FeatureLoadException {
        val message = when (exception.code()) {
            401 -> {
                tokenManager.clearAuthState()
                "Session expired. Please sign in again."
            }
            403 -> "You do not have permission to view this data."
            404 -> "No rewards, safety scores, or alerts were found."
            500 -> "Server error. Please try again later."
            else -> "Unable to load data right now."
        }
        return FeatureLoadException(message)
    }
}

private class FeatureLoadException(message: String) : IllegalStateException(message)

