package com.ackileo.telematics.data.remote
import com.ackileo.telematics.data.remote.models.AuthResponse
import com.ackileo.telematics.data.remote.models.EventRequest
import com.ackileo.telematics.data.remote.models.LoginRequest
import com.ackileo.telematics.data.remote.models.RegisterRequest
import com.ackileo.telematics.data.remote.models.TripEndRequest
import com.ackileo.telematics.data.remote.models.TripResponse
import com.ackileo.telematics.data.remote.models.TripStartRequest
import com.ackileo.telematics.domain.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST





interface ApiService {
    // Replace this with your actual API endpoints
    // Inside ApiService.kt
    @GET("trips")
    suspend fun getTrips(): List<TripResponse>


    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("Drivers/profile")
    suspend fun getProfile(): Driver

    @POST("trips/start")
    suspend fun startTrip(@Body request: TripStartRequest): TripResponse

    @POST("trips/end")
    suspend fun endTrip(@Body request: TripEndRequest): TripResponse

    @POST("events")
    suspend fun sendEvent(@Body request: EventRequest)

    @GET("scores/current")
    suspend fun getCurrentScore(): SafetyScore

    @GET("rewards")
    suspend fun getRewards(): List<Reward>

}