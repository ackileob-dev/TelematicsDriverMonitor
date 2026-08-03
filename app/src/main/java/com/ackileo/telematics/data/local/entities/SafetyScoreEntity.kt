package com.ackileo.telematics.data.local.entities

import androidx.room.Entity


import androidx.room.PrimaryKey

@Entity(tableName = "safety_scores")
data class SafetyScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val score: Double,
    val timestamp: Long, // Use 'timestamp' instead of 'date' for better SQLite compatibility
    val driveDate: String, // If you need a readable date like "2023-10-25"
)