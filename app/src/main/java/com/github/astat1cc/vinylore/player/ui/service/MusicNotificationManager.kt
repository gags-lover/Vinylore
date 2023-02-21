package com.github.astat1cc.vinylore.player.ui.service

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.astat1cc.vinylore.R
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class MusicNotificationManager(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener
) {

    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController =
            MediaControllerCompat(context, sessionToken) // todo inject here and everywhere
        notificationManager =
            PlayerNotificationManager.Builder(context, NOTIFICATION_ID, CHANNEL_ID)
                .setNotificationListener(notificationListener)
                .setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
                .setChannelNameResourceId(R.string.mock1) // todo remove mocks
                .setChannelDescriptionResourceId(R.string.mock2)
                .build().apply {
                    setMediaSessionToken(sessionToken)
                    setSmallIcon(R.drawable.ic_music_note) // todo change icon
                    setUseRewindAction(false)
                    setUseFastForwardAction(false)
                    setUseNextActionInCompactView(true)
                    setUsePreviousActionInCompactView(true)
                }
    }

    fun showNotification(player: Player) {
        notificationManager.setPlayer(player)
    }

    fun hideNotification() {
        notificationManager.setPlayer(null)
    }

    private inner class DescriptionAdapter(
        private val mediaController: MediaControllerCompat
    ) : PlayerNotificationManager.MediaDescriptionAdapter {

        override fun getCurrentContentTitle(player: Player): CharSequence =
            mediaController.metadata.description.title.toString()

        override fun createCurrentContentIntent(player: Player): PendingIntent? =
            mediaController.sessionActivity

        override fun getCurrentContentText(player: Player): CharSequence? =
            mediaController.metadata.description.subtitle

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            val foundAlbumCover = mediaController.metadata.description.iconBitmap
            Glide.with(context).asBitmap()
                .load(
                    foundAlbumCover ?: R.drawable.album_cover_vinylore
                ) // todo make actual album cover
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        callback.onBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) = Unit
                })
            return null  // todo song's big icon
        }
    }

    companion object {

        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "1"
    }
}