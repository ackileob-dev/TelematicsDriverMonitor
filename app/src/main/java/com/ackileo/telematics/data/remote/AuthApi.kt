package com.ackileo.telematics.data.remote

import com.ackileo.telematics.data.remote.dto.DriverDto
import com.ackileo.telematics.data.remote.dto.LoginRequest
import com.ackileo.telematics.data.remote.dto.LoginResponse
import com.ackileo.telematics.data.remote.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<Any>>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<DriverDto>
}

