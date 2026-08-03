package com.ackileo.telematics.utils



import android.app.NotificationChannel
import android.app.NotificationManager

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.ackileo.telematics.R

import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ALERTS = "driving_alerts"
        const val CHANNEL_REWARDS = "reward_milestones"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alertChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "Driving Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Alerts for overSpeeding and harsh driving" }

            val rewardChannel = NotificationChannel(
                CHANNEL_REWARDS,
                "Rewards & Milestones",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Notifications for safety score achievements" }

            notificationManager.createNotificationChannel(alertChannel)
            notificationManager.createNotificationChannel(rewardChannel)
        }
    }

    fun showNotification(title: String, message: String, channelId: String) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Ensure this icon exists
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}