

package com.ackileo.telematics.domain.model

import java.time.LocalDateTime

data class Reward(
    val id: String,
    val title: String,
    val description: String,
    val pointsRequired: Int,
    val isRedeemed: Boolean,
    val expiryDate: LocalDateTime?,
    val partnerName: String // e.g., "Shell", "Insurance Partner"
)