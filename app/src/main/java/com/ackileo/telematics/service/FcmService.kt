package com.ackileo.telematics.service
import com.ackileo.telematics.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Handle Data payloads (Best for background processing)
        remoteMessage.data.let { data ->
            val type = data["type"] ?: "DEFAULT"
            val title = data["title"] ?: "Safety Update"
            val body = data["body"] ?: ""

            val channelId = when (type) {
                "OVER_SPEED", "DANGEROUS_DRIVING" -> NotificationHelper.CHANNEL_ALERTS
                "REWARD", "MILESTONE" -> NotificationHelper.CHANNEL_REWARDS
                else -> NotificationHelper.CHANNEL_ALERTS
            }

            notificationHelper.showNotification(title, body, channelId)
        }
    }

}