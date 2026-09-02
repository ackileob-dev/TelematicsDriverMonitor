package com.ackileo.telematics.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.ackileo.telematics.data.remote.dto.RewardDto
import com.ackileo.telematics.domain.model.Reward
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
fun RewardDto.toDomain(): Reward {
    return Reward(
        id = id,
        title = title,
        description = description.orEmpty(),
        pointsRequired = points,
        isRedeemed = isRedeemed ?: false,
        expiryDate = issuedAt?.let {
            runCatching {
                LocalDateTime.parse(it)
            }.getOrNull()
        },
        partnerName = "Partner"
    )
}