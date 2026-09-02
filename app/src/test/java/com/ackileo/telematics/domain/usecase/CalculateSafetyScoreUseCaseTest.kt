package com.ackileo.telematics.domain.usecase

import com.ackileo.telematics.domain.model.usecase.CalculateSafetyScoreUseCase
import org.junit.Assert.assertEquals // Required import
import org.junit.Test               // Required import

class CalculateSafetyScoreUseCaseTest {

    private val useCase = CalculateSafetyScoreUseCase()

    @Test
    fun testWhenNoNegativeEventsScoreShouldBe100() {
        val score = useCase(0, 0, 0, 0)
        assertEquals(100, score)
    }

    @Test
    fun testWhenEventsOccurScoreShouldDecreaseAccordingly() {
        // Implemented logic: overspeed -10, harsh brake -5, rapid accel -5, phone use -15
        // 100 - (10 + 5 + 5 + 15) = 65
        val score = useCase(
            overSpeedCount = 1,
            harshBrakeCount = 1,
            rapidAccelCount = 1,
            phoneUsageCount = 1
        )
        assertEquals(65, score)
    }

    @Test
    fun testScoreShouldNotDropBelow0() {
        val score = useCase(100, 100, 100, 100)
        assertEquals(0, score)
    }

    @Test
    fun testSingleEventOverspeedPenaltyApplied() {
        val score = useCase(overSpeedCount = 1, harshBrakeCount = 0, rapidAccelCount = 0, phoneUsageCount = 0)
        assertEquals(90, score)
    }

    @Test
    fun testRepeatedEventsCombineDeterministically() {
        // 100 - (2*10) - (3*5) - (1*5) - (1*15) = 45
        val score = useCase(overSpeedCount = 2, harshBrakeCount = 3, rapidAccelCount = 1, phoneUsageCount = 1)
        assertEquals(45, score)
    }

    @Test
    fun testScoreUpperBoundRemains100() {
        val score = useCase(overSpeedCount = 0, harshBrakeCount = 0, rapidAccelCount = 0, phoneUsageCount = 0)
        assertEquals(100, score)
    }

    @Test
    fun testScoreLowerBoundRemains0() {
        val score = useCase(overSpeedCount = 9, harshBrakeCount = 9, rapidAccelCount = 9, phoneUsageCount = 9)
        assertEquals(0, score)
    }
}