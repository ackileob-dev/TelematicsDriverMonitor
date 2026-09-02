package com.ackileo.telematics.data.remote
import com.ackileo.telematics.data.remote.dto.AlertDto
import com.ackileo.telematics.data.remote.dto.DriverDto
import com.ackileo.telematics.data.remote.dto.DrivingEventCreateRequest
import com.ackileo.telematics.data.remote.dto.DrivingEventDto
import com.ackileo.telematics.data.remote.dto.LoginRequest as DtoLoginRequest
import com.ackileo.telematics.data.remote.dto.LoginResponse
import com.ackileo.telematics.data.remote.dto.RegisterRequest as DtoRegisterRequest
import com.ackileo.telematics.data.remote.dto.RewardDto
import com.ackileo.telematics.data.remote.dto.SafetyScoreDto
import com.ackileo.telematics.data.remote.dto.TripCreateRequest
import com.ackileo.telematics.data.remote.dto.TripDto
import com.ackileo.telematics.data.remote.dto.TripUpdateRequest
import com.ackileo.telematics.data.remote.dto.VehicleDto
import com.ackileo.telematics.data.remote.models.AuthResponse
import com.ackileo.telematics.data.remote.models.EventRequest
import com.ackileo.telematics.data.remote.models.LoginRequest as LegacyLoginRequest
import com.ackileo.telematics.data.remote.models.RegisterRequest as LegacyRegisterRequest
import com.ackileo.telematics.data.remote.models.TripEndRequest
import com.ackileo.telematics.data.remote.models.TripResponse
import com.ackileo.telematics.data.remote.models.TripStartRequest
import com.ackileo.telematics.domain.model.Driver
import com.ackileo.telematics.domain.model.Reward
import com.ackileo.telematics.domain.model.SafetyScore
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Response





interface ApiService {
    // Authentication
    @POST("auth/register")
    suspend fun register(@Body request: DtoRegisterRequest): Response<ApiResponse<LoginResponse>>

    @POST("auth/login")
    suspend fun login(@Body request: DtoLoginRequest): Response<ApiResponse<LoginResponse>>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<ApiResponse<DriverDto>>

    // Drivers
    @GET("drivers")
    suspend fun getDrivers(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("search") search: String? = null,
    ): ApiResponse<List<DriverDto>>

    @GET("drivers/{id}")
    suspend fun getDriver(
        @Path("id") id: String,
    ): ApiResponse<DriverDto>

    @POST("drivers")
    suspend fun createDriver(
        @Body driver: DriverDto,
    ): ApiResponse<DriverDto>

    @PUT("drivers/{id}")
    suspend fun updateDriver(
        @Path("id") id: String,
        @Body driver: DriverDto,
    ): ApiResponse<DriverDto>

    @DELETE("drivers/{id}")
    suspend fun deleteDriver(
        @Path("id") id: String,
    ): ApiResponse<Any>

    // Vehicles
    @GET("vehicles")
    suspend fun getVehicles(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("driverId") driverId: String? = null,
    ): ApiResponse<List<VehicleDto>>

    @GET("vehicles/{id}")
    suspend fun getVehicle(
        @Path("id") id: String,
    ): ApiResponse<VehicleDto>

    @POST("vehicles")
    suspend fun createVehicle(
        @Body vehicle: VehicleDto,
    ): ApiResponse<VehicleDto>

    @PUT("vehicles/{id}")
    suspend fun updateVehicle(
        @Path("id") id: String,
        @Body vehicle: VehicleDto,
    ): ApiResponse<VehicleDto>

    @DELETE("vehicles/{id}")
    suspend fun deleteVehicle(
        @Path("id") id: String,
    ): ApiResponse<Any>

    // Trips
    @GET("trips")
    suspend fun getTrips(
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
        @Query("driverId") driverId: String?,
        @Query("vehicleId") vehicleId: String?,
        @Query("status") status: String?,
    ): ApiResponse<List<TripDto>>

    @GET("trips/{id}")
    suspend fun getTrip(
        @Path("id") id: String,
    ): ApiResponse<TripDto>

    @POST("trips")
    suspend fun createTrip(
        @Body trip: TripDto,
    ): ApiResponse<TripDto>

    @POST("trips")
    suspend fun createTripSession(
        @Body request: TripCreateRequest,
    ): ApiResponse<TripDto>

    @PUT("trips/{id}")
    suspend fun updateTrip(
        @Path("id") id: String,
        @Body trip: TripDto,
    ): ApiResponse<TripDto>

    @PUT("trips/{id}")
    suspend fun completeTripSession(
        @Path("id") id: String,
        @Body request: TripUpdateRequest,
    ): ApiResponse<TripDto>

    @DELETE("trips/{id}")
    suspend fun deleteTrip(
        @Path("id") id: String,
    ): ApiResponse<Any>

    // Driving events
    @GET("events")
    suspend fun getDrivingEvents(
        @Query("tripId") tripId: String? = null,
        @Query("type") type: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
    ): ApiResponse<List<DrivingEventDto>>

    @GET("events/{id}")
    suspend fun getDrivingEvent(
        @Path("id") id: String,
    ): ApiResponse<DrivingEventDto>

    @POST("events")
    suspend fun createDrivingEvent(
        @Body event: DrivingEventDto,
    ): ApiResponse<DrivingEventDto>

    @POST("driving-events")
    suspend fun postDrivingEvent(
        @Body event: DrivingEventCreateRequest,
    ): ApiResponse<DrivingEventDto>

    // Safety scores
    @GET("safety-scores")
    suspend fun getSafetyScores(
        @Query("driverId") driverId: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
    ): ApiResponse<List<SafetyScoreDto>>

    @GET("safety-scores/{id}")
    suspend fun getSafetyScore(
        @Path("id") id: String,
    ): ApiResponse<SafetyScoreDto>

    // Rewards
    @GET("rewards")
    suspend fun getRewards(
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
        @Query("driverId") driverId: String?,
    ): ApiResponse<List<RewardDto>>

    @GET("rewards/{id}")
    suspend fun getReward(
        @Path("id") id: String,
    ): ApiResponse<RewardDto>

    // Alerts
    @GET("alerts")
    suspend fun getAlerts(
        @Query("driverId") driverId: String? = null,
        @Query("level") level: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
    ): ApiResponse<List<AlertDto>>

    @GET("alerts/{id}")
    suspend fun getAlert(
        @Path("id") id: String,
    ): ApiResponse<AlertDto>

    @POST("alerts")
    suspend fun createAlert(
        @Body alert: AlertDto,
    ): ApiResponse<AlertDto>

    @PATCH("alerts/{id}")
    suspend fun updateAlert(
        @Path("id") id: String,
        @Body alert: AlertDto,
    ): ApiResponse<AlertDto>

    // ── Legacy endpoints ──────────────────────────────────────────────────────
    // These use the older flat response models (no ApiResponse wrapper) and are
    // retained for TripRepository / TrackingRepository compatibility.
    // The no-arg getRewards() overload has been renamed to avoid confusion with
    // the paginated getRewards(page, limit, driverId) declared above.

    @POST("auth/register")
    suspend fun registerLegacy(@Body request: LegacyRegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun loginLegacy(@Body request: LegacyLoginRequest): AuthResponse

    @GET("drivers/profile")
    suspend fun getProfile(): Driver

    @POST("trips/start")
    suspend fun startTrip(@Body request: TripStartRequest): TripResponse

    @POST("trips/end")
    suspend fun endTrip(@Body request: TripEndRequest): TripResponse

    @POST("events")
    suspend fun sendEvent(@Body request: EventRequest)

    @GET("scores/current")
    suspend fun getCurrentScore(): SafetyScore

    // Renamed from getRewards() to prevent Kotlin overload resolution ambiguity
    // with the paginated getRewards(page, limit, driverId) above.
    @GET("rewards")
    suspend fun getAllRewardsLegacy(): List<Reward>

}
