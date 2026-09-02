package com.ackileo.telematics.data.remote

import com.ackileo.telematics.data.remote.dto.TripDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface TripApi {

    @POST("trips/start")
    suspend fun startTrip(): Response<ApiResponse<Any>>

    @POST("trips/end")
    suspend fun endTrip(): Response<ApiResponse<Any>>

    @GET("trips")
    suspend fun getTrips(): Response<List<TripDto>>
}

