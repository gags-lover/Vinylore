package com.github.astat1cc.vinylore.player.ui.service

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator

class MediaQueueNavigator(
    private val mediaSource: MusicMediaSource,
    mediaSessionCompat: MediaSessionCompat
) : TimelineQueueNavigator(mediaSessionCompat) {

    override fun getMediaDescription(
        player: Player,
        windowIndex: Int
    ): MediaDescriptionCompat {
        if (windowIndex < mediaSource.audioMediaMetadata.size) {
            return mediaSource.audioMediaMetadata[windowIndex].description
        }

        return MediaDescriptionCompat.Builder().build()
    }
}