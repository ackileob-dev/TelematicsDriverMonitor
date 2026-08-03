package com.ackileo.telematics.data.repository



import com.ackileo.telematics.data.remote.ApiService
import com.ackileo.telematics.domain.model.Reward
import javax.inject.Inject
import javax.inject.Singleton

interface RewardRepository {
    suspend fun getRewards(): Result<List<Reward>>
}

@Singleton
class RewardRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : RewardRepository {

    override suspend fun getRewards(): Result<List<Reward>> {
        return try {
            val rewards = apiService.getRewards()
            Result.success(rewards)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}