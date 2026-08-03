package com.ackileo.telematics.domain.model

import java.time.LocalDateTime

/**
 * Represents the safety performance metrics for a driver.
 * Scores are expected to be in the range of 0 to 100.
 */
data class SafetyScore(
    val driverId: String,
    val overallScore: Int,
    val smoothDrivingScore: Int,
    val speedingScore: Int,
    val focusScore: Int,
    val lastUpdated: LocalDateTime,
    val trend: ScoreTrend
) {
    enum class ScoreTrend {
        UP,
        DOWN,
        STABLE,
        UNKNOWN
    }
}