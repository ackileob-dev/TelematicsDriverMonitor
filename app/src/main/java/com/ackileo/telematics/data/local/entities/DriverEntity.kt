package com.ackileo.telematics.data.local.entities


/**
 * Represents the authenticated driver currently using the device.
 */
@androidx.room.Entity(tableName = "Driver_Profile") // FIXED: Matches your DAO query
data class DriverEntity(
    @androidx.room.PrimaryKey
    val driverId: String,

    val email: String,

    val fullName: String,

    val phoneNumber: String? = null,

    val profilePictureUrl: String? = null,

    val isActive: Boolean = true,

    val lastSyncTimestamp: Long = System.currentTimeMillis(),
)
/**
 * Represents the Driver's profile information in the local database.
 *
 * IMPORTANT: The tableName is set to "Driver_Profile" to match the SQL queries
 * in your DriverDao and resolve the KSP build error.
 */
