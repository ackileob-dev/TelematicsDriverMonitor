package com.ackileo.telematics.data.repository

import com.ackileo.telematics.data.remote.ApiService
import com.ackileo.telematics.domain.model.Driver
import com.ackileo.telematics.domain.model.SafetyScore
import javax.inject.Inject
import javax.inject.Singleton

interface DriverRepository {
    suspend fun getProfile(): Result<Driver>
    suspend fun getCurrentScore(): Result<SafetyScore>
}

@Singleton
class DriverRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : DriverRepository {

    override suspend fun getProfile(): Result<Driver> {
        return try {
            val profile = apiService.getProfile()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentScore(): Result<SafetyScore> {
        return try {
            val score = apiService.getCurrentScore()
            Result.success(score)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}