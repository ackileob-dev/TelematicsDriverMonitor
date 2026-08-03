package com.ackileo.telematics.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ackileo.telematics.data.local.dao.DriverDao
import com.ackileo.telematics.data.local.dao.TripDao // Fixed typo: Dao instead of Doa
import com.ackileo.telematics.data.local.entities.DriverEntity
import com.ackileo.telematics.data.local.entities.SafetyScoreEntity
import com.ackileo.telematics.data.local.entities.TripEntity

@Database(
    entities = [
        DriverEntity::class,
        TripEntity::class,
        SafetyScoreEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun driverDao(): DriverDao
    abstract fun tripDao(): TripDao // Fixed typo: Dao instead of Doa
}