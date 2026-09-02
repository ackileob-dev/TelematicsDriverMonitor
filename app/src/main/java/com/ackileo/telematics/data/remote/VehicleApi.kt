package com.ackileo.telematics.data.remote

import com.ackileo.telematics.data.remote.dto.VehicleDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface VehicleApi {

    @GET("vehicles")
    suspend fun getVehicles(): Response<List<VehicleDto>>

    @POST("vehicles")
    suspend fun addVehicle(
        @Body vehicle: VehicleDto
    ): Response<VehicleDto>
}

