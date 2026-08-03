package com.ackileo.telematics.data.local.entities




import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single trip record stored in the local SQLite database.
 * Table name: "trips"
 */
@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val startTime: Long,

    val endTime: Long,

    val totalDistanceKm: Double,

    val averageSpeedKmh: Double,

    val durationMinutes: Int,

    val safetyScore: Double,
    val destination: String,
    /**
     * The destination address or coordinates of the trip.
     * Defaulted to an empty string or "Unknown" as per the repository mapper.
     */
    val isSynced: Boolean = false


)


