package com.ackileo.telematics.data.remote

import com.ackileo.telematics.data.remote.dto.DriverDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface DriverApi {

    @GET("drivers/profile")
    suspend fun getProfile(): Response<DriverDto>

    @PUT("drivers/profile")
    suspend fun updateProfile(
        @Body driver: DriverDto
    ): Response<DriverDto>
}

