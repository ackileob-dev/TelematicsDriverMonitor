package com.ackileo.telematics.data.local.dao

import androidx.room.*
import com.ackileo.telematics.data.local.entities.DriverEntity
import com.ackileo.telematics.data.local.entities.SafetyScoreEntity
import com.ackileo.telematics.data.local.entities.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DriverDao {

    // --- Driver Profile ---

    /**
     * Note: Ensure DriverEntity has @Entity(tableName = "Driver_Profile")
     */
    @Query("SELECT * FROM Driver_Profile LIMIT 1")
    fun getProfile(): Flow<DriverEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: DriverEntity)

    @Query("DELETE FROM Driver_Profile")
    suspend fun clearProfile()

    // --- Trips ---

    @Query("SELECT * FROM trips ORDER BY startTime DESC")
    fun getRecentTrips(): Flow<List<TripEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<TripEntity>)

    @Query("DELETE FROM trips")
    suspend fun clearAllTrips()

    // --- Safety Scores ---

    @Query("SELECT * FROM safety_scores ORDER BY timestamp DESC LIMIT 30")
    fun getSafetyHistory(): Flow<List<SafetyScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: SafetyScoreEntity)

    // --- Atomic Transactions ---

    /**
     * Refreshes the local trip cache by deleting old records and inserting new ones
     * in a single atomic operation to prevent UI flickering.
     */
    @Transaction
    suspend fun refreshTrips(trips: List<TripEntity>) {
        clearAllTrips()
        insertTrips(trips)
    }
    // In TripDao.kt
    @Query("SELECT * FROM trips WHERE isSynced = 0") // 0 means false
    suspend fun getUnsyncedTrips(): List<TripEntity>

    @Query("UPDATE trips SET isSynced = 1 WHERE id = :tripId") // 1 means true
    suspend fun markAsSynced(tripId: Long)
}