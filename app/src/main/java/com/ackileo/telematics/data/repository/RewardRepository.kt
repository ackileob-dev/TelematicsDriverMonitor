package com.ackileo.telematics.data.repository



import com.ackileo.telematics.data.remote.ApiResponse
import com.ackileo.telematics.data.remote.ApiService
import com.ackileo.telematics.data.remote.dto.RewardDto
import javax.inject.Inject
import javax.inject.Singleton



interface RewardRepository {
    suspend fun getRewards(rewards: ApiResponse<List<RewardDto>>): Result<ApiResponse<List<RewardDto>>>
}

@Singleton
class RewardRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : RewardRepository {

    override suspend fun getRewards(rewards: ApiResponse<List<RewardDto>>): Result<ApiResponse<List<RewardDto>>> {
        return try {
            Result.success(rewards)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}