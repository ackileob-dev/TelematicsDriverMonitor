package com.ackileo.telematics.domain.usecase

import com.ackileo.telematics.domain.model.usecase.CalculateSafetyScoreUseCase
import org.junit.Assert.assertEquals // Required import
import org.junit.Test               // Required import

class CalculateSafetyScoreUseCaseTest {

    private val useCase = CalculateSafetyScoreUseCase()

    @Test
    fun `when no negative events, score should be 100`() {
        val score = useCase(0, 0, 0, 0)
        assertEquals(100, score)
    }

    @Test
    fun `when events occur, score should decrease accordingly`() {
        // Logic: 100 - (1*5) - (1*10) - (1*10) - (1*15) = 60
        val score = useCase(
            overspeedCount = 1,
            harshBrakeCount = 1,
            rapidAccelCount = 1,
            phoneUsageCount = 1
        )
        assertEquals(60, score)
    }

    @Test
    fun `score should not drop below 0`() {
        val score = useCase(100, 100, 100, 100)
        assertEquals(0, score)
    }
}