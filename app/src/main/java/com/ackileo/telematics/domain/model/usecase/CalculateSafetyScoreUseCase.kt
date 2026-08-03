package com.ackileo.telematics.domain.model.usecase



import javax.inject.Inject

/**
 * Use Case to calculate the driver's safety score based on telematics events.
 *
 * Logic:
 * - Starting Score: 100
 * - OverSpeeding: -10 points per event
 * - Harsh Braking: -5 points per event
 * - Rapid Acceleration: -5 points per event
 * - Phone Usage: -15 points per event
 * - Range: [0 - 100]
 */
class CalculateSafetyScoreUseCase @Inject constructor() {

    operator fun invoke(
        overSpeedCount: Int,
        harshBrakeCount: Int,
        rapidAccelCount: Int,
        phoneUsageCount: Int
    ): Int {
        val initialScore = 100

        val totalDeduction = (overSpeedCount * 10) +
                (harshBrakeCount * 5) +
                (rapidAccelCount * 5) +
                (phoneUsageCount * 15)

        val finalScore = initialScore - totalDeduction

        // Ensures the score does not drop below 0 or exceed 100
        return finalScore.coerceIn(0, 100)
    }
}