package com.github.astat1cc.vinylore.player.ui.service

import android.app.Notification
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class MusicPlayerNotificationListener(
    private val musicService: MusicService
) : PlayerNotificationManager.NotificationListener {

    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        super.onNotificationCancelled(notificationId, dismissedByUser)

        musicService.apply {
            stopCrackle()
            stopForeground(true) // todo deprecated
            isForegroundService = false
            stopSelf() // todo isn't it duplicating?
        }
    }

    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        musicService.apply {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                    applicationContext,
                    Intent(applicationContext, MusicService::class.java)
                )
                startForeground(notificationId, notification)
                isForegroundService = true
            }
        }
    }
}