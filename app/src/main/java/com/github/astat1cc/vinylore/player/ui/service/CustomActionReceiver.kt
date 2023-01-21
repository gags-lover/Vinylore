package com.github.astat1cc.vinylore.player.ui.service

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class CustomActionReceiver : PlayerNotificationManager.CustomActionReceiver {

    override fun createCustomActions(
        context: Context,
        instanceId: Int
    ): MutableMap<String, NotificationCompat.Action> {
        TODO("Not yet implemented")
    }

    override fun getCustomActions(player: Player): MutableList<String> {
        TODO("Not yet implemented")
    }

    override fun onCustomAction(player: Player, action: String, intent: Intent) {
        TODO("Not yet implemented")
    }
}