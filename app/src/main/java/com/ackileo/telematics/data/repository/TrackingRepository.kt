package com.ackileo.telematics.data.repository

import com.ackileo.telematics.data.local.TokenManager
import com.ackileo.telematics.data.remote.ApiService
import com.ackileo.telematics.data.remote.dto.DrivingEventCreateRequest
import com.ackileo.telematics.data.remote.dto.TripCreateRequest
import com.ackileo.telematics.data.remote.dto.TripLocationDto
import com.ackileo.telematics.data.remote.dto.TripUpdateRequest
import com.ackileo.telematics.utils.DrivingEvent
import com.ackileo.telematics.utils.LocationData
import com.ackileo.telematics.utils.SensorTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

data class TripTrackingSession(
	val tripId: String,
	val driverId: String?,
	val startedAtMillis: Long,
)

data class TrackingEventRecord(
	val eventType: String,
	val timestampIso: String,
)

interface TrackingRepository {
	suspend fun startTripSession(startLocation: LocationData? = null): Result<TripTrackingSession>
	suspend fun endTripSession(
		session: TripTrackingSession,
		locationData: LocationData,
		maxSpeedKmh: Float,
		safetyScore: Double? = null,
	): Result<Unit>

	suspend fun processTelemetry(
		session: TripTrackingSession,
		locationData: LocationData,
		sensorTelemetry: SensorTelemetry,
		event: DrivingEvent,
	): Result<TrackingEventRecord?>
}

@Singleton
class TrackingRepositoryImpl @Inject constructor(
	private val apiService: ApiService,
	private val tokenManager: TokenManager,
) : TrackingRepository {

	private val pendingQueue = ArrayDeque<DrivingEventCreateRequest>()
	private val lastSentByTypeEpochMs = ConcurrentHashMap<String, Long>()
	private var lastSentEpochMs: Long = 0L

	override suspend fun startTripSession(startLocation: LocationData?): Result<TripTrackingSession> = withContext(Dispatchers.IO) {
		try {
			val driverId = tokenManager.getDriverId()

			val activeTripId = tokenManager.getActiveTripId()
			val activeTripStartedAt = tokenManager.getActiveTripStartedAtMillis()
			if (activeTripId != null && activeTripStartedAt != null) {
				return@withContext Result.success(
					TripTrackingSession(
						tripId = activeTripId,
						driverId = driverId,
						startedAtMillis = activeTripStartedAt,
					)
				)
			}

			val vehicleId = resolveVehicleId(driverId)
				?: throw TrackingException("No linked vehicle found. Please add a vehicle before tracking.")

			val now = System.currentTimeMillis()
			val response = apiService.createTripSession(
				TripCreateRequest(
					driverId = driverId,
					vehicleId = vehicleId,
					startTimestamp = utcTimestamp(now),
					startLocation = startLocation
						?.takeIf { isValidCoordinate(it.latitude, it.longitude) }
						?.toTripLocationDto(),
				)
			)

			val tripId = response.data?.id?.takeIf { it.isNotBlank() }
				?: throw TrackingException("Trip started but no trip id was returned by backend.")

			tokenManager.saveActiveTrip(tripId, now)

			Result.success(
				TripTrackingSession(
					tripId = tripId,
					driverId = driverId,
					startedAtMillis = now,
				)
			)
		} catch (e: Exception) {
			Result.failure(mapTripError("Unable to start trip", e))
		}
	}

	override suspend fun endTripSession(
		session: TripTrackingSession,
		locationData: LocationData,
		maxSpeedKmh: Float,
		safetyScore: Double?,
	): Result<Unit> = withContext(Dispatchers.IO) {
		try {
			val now = System.currentTimeMillis()
			val durationMillis = (now - session.startedAtMillis).coerceAtLeast(1L)
			val durationSeconds = (durationMillis / 1000L).toInt()
			val durationHours = durationMillis / 3_600_000.0
			val avgSpeed = if (durationHours > 0.0) locationData.totalDistanceKm / durationHours else 0.0

			apiService.completeTripSession(
				id = session.tripId,
				request = TripUpdateRequest(
					endTimestamp = utcTimestamp(now),
					endLocation = locationData
						.takeIf { isValidCoordinate(it.latitude, it.longitude) }
						?.toTripLocationDto(),
					distance = locationData.totalDistanceKm,
					duration = durationSeconds,
					averageSpeed = avgSpeed,
					maximumSpeed = maxSpeedKmh.toDouble(),
					safetyScore = safetyScore,
				),
			)

			tokenManager.clearActiveTrip()
			Result.success(Unit)
		} catch (e: Exception) {
			Result.failure(mapTripError("Unable to complete trip", e))
		}
	}

	override suspend fun processTelemetry(
		session: TripTrackingSession,
		locationData: LocationData,
		sensorTelemetry: SensorTelemetry,
		event: DrivingEvent,
	): Result<TrackingEventRecord?> = withContext(Dispatchers.IO) {
		try {
			flushPendingEvents()

			val detectedType = detectTrackableEventType(locationData, sensorTelemetry, event) ?: return@withContext Result.success(null)
			if (!isValidCoordinate(locationData.latitude, locationData.longitude)) return@withContext Result.success(null)
			if (!isValidAccuracy(locationData.accuracyMeters)) return@withContext Result.success(null)
			if (!shouldSendEvent(detectedType)) return@withContext Result.success(null)

			val timestampMillis = locationData.timestampMillis.takeIf { it > 0L } ?: System.currentTimeMillis()
			val payload = DrivingEventCreateRequest(
				tripId = session.tripId,
				driverId = session.driverId,
				type = detectedType,
				timestamp = utcTimestamp(timestampMillis),
				latitude = locationData.latitude,
				longitude = locationData.longitude,
				gpsSpeed = locationData.speedKmh.toDouble(),
				accuracy = locationData.accuracyMeters,
				accelerometerX = sensorTelemetry.accelerometerX,
				accelerometerY = sensorTelemetry.accelerometerY,
				accelerometerZ = sensorTelemetry.accelerometerZ,
				severity = mapSeverity(detectedType, locationData.speedKmh),
			)

			sendOrQueue(payload)

			Result.success(
				TrackingEventRecord(
					eventType = detectedType,
					timestampIso = payload.timestamp,
				)
			)
		} catch (e: Exception) {
			Result.failure(mapTripError("Failed to process driving event", e))
		}
	}

	private suspend fun resolveVehicleId(driverId: String?): String? {
		return try {
			apiService.getVehicles(page = 1, limit = 1, driverId = driverId).data?.firstOrNull()?.id
		} catch (_: Exception) {
			null
		}
	}

	private suspend fun sendOrQueue(payload: DrivingEventCreateRequest) {
		try {
			apiService.postDrivingEvent(payload)
			markSent(payload.type)
		} catch (_: IOException) {
			queueEvent(payload)
		} catch (_: Exception) {
			queueEvent(payload)
		}
	}

	private suspend fun flushPendingEvents() {
		if (pendingQueue.isEmpty()) return

		val stillPending = ArrayDeque<DrivingEventCreateRequest>()
		while (pendingQueue.isNotEmpty()) {
			val queued = pendingQueue.removeFirst()
			try {
				apiService.postDrivingEvent(queued)
				markSent(queued.type)
			} catch (_: Exception) {
				stillPending.addLast(queued)
			}
		}

		pendingQueue.addAll(stillPending.take(MAX_PENDING_EVENTS))
	}

	private fun queueEvent(payload: DrivingEventCreateRequest) {
		if (pendingQueue.size >= MAX_PENDING_EVENTS) {
			pendingQueue.removeFirst()
		}
		pendingQueue.addLast(payload)
	}

	private fun detectTrackableEventType(
		locationData: LocationData,
		sensorTelemetry: SensorTelemetry,
		event: DrivingEvent,
	): String? {
		return when {
			locationData.speedKmh >= SPEEDING_THRESHOLD_KMH -> "speeding"
			event == DrivingEvent.HARSH_BRAKING -> "harsh_braking"
			event == DrivingEvent.RAPID_ACCELERATION -> "harsh_acceleration"
			event == DrivingEvent.SHARP_CORNERING -> "sharp_cornering"
			event == DrivingEvent.PHONE_USAGE -> "phone_distraction"
			sensorTelemetry.accelerometerX?.let { abs(it) >= SHARP_CORNERING_ACCEL_THRESHOLD } == true -> "sharp_cornering"
			else -> null
		}
	}

	private fun shouldSendEvent(eventType: String): Boolean {
		val now = System.currentTimeMillis()
		if (now - lastSentEpochMs < MIN_GLOBAL_SEND_INTERVAL_MS) return false

		val lastForType = lastSentByTypeEpochMs[eventType] ?: 0L
		return now - lastForType >= MIN_EVENT_TYPE_INTERVAL_MS
	}

	private fun markSent(eventType: String) {
		val now = System.currentTimeMillis()
		lastSentEpochMs = now
		lastSentByTypeEpochMs[eventType] = now
	}

	private fun isValidCoordinate(latitude: Double, longitude: Double): Boolean {
		val isLatitudeValid = latitude.isFinite() && latitude in -90.0..90.0
		val isLongitudeValid = longitude.isFinite() && longitude in -180.0..180.0
		return isLatitudeValid && isLongitudeValid
	}

	private fun isValidAccuracy(accuracy: Float): Boolean {
		return accuracy.isFinite() && accuracy in 0f..MAX_ACCEPTED_ACCURACY_METERS
	}

	private fun mapSeverity(eventType: String, speedKmh: Float): String {
		return when (eventType) {
			"speeding" -> if (speedKmh > 100f) "high" else "medium"
			"phone_distraction" -> "high"
			"harsh_braking", "harsh_acceleration", "sharp_cornering" -> "medium"
			else -> "low"
		}
	}

	private fun utcTimestamp(epochMillis: Long): String {
		val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
		formatter.timeZone = TimeZone.getTimeZone("UTC")
		return formatter.format(Date(epochMillis))
	}

	private fun LocationData.toTripLocationDto(): TripLocationDto {
		return TripLocationDto(
			latitude = latitude,
			longitude = longitude,
			accuracy = accuracyMeters.takeIf(::isValidAccuracy),
		)
	}

	private fun mapTripError(fallback: String, throwable: Exception): Exception {
		return when (throwable) {
			is HttpException -> {
				if (throwable.code() == 401) {
					tokenManager.clearAuthState()
				}
				TrackingException(
					when (throwable.code()) {
						401 -> "Session expired. Please sign in again."
						409 -> "Trip state conflict detected. Please refresh and retry."
						500 -> "Server error while syncing trip."
						else -> fallback
					}
				)
			}

			is IOException -> TrackingException("Network error. Trip data will retry when possible.")
			is TrackingException -> throwable
			else -> TrackingException(throwable.message ?: fallback)
		}
	}

	private companion object {
		const val SPEEDING_THRESHOLD_KMH = 85f
		const val SHARP_CORNERING_ACCEL_THRESHOLD = 3.0f
		const val MIN_GLOBAL_SEND_INTERVAL_MS = 1_000L
		const val MIN_EVENT_TYPE_INTERVAL_MS = 5_000L
		const val MAX_PENDING_EVENTS = 100
		const val MAX_ACCEPTED_ACCURACY_METERS = 200f
	}
}

private class TrackingException(message: String) : IllegalStateException(message)
