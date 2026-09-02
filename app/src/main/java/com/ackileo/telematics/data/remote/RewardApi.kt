package com.ackileo.telematics.data.remote

import com.ackileo.telematics.data.remote.dto.RewardDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface RewardApi {

    @GET("rewards")
    suspend fun getRewards(): Response<List<RewardDto>>

    @POST("rewards/redeem")
    suspend fun redeem(): Response<ApiResponse<Any>>
}

