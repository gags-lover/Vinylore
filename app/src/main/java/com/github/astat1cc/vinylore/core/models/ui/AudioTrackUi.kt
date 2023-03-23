package com.github.astat1cc.vinylore.core.models.ui

import android.graphics.Bitmap
import android.net.Uri
import com.github.astat1cc.vinylore.core.models.domain.AppAudioTrack

data class AudioTrackUi(
    val uri: Uri,
    val title: String,
    val artist: String?,
    val duration: Long,
    val albumCover: Bitmap?,
    val sampleRate: String?,
    val mediaFormat: String?,
    val bitrate: String?
) {

    companion object {

        fun fromDomain(track: AppAudioTrack) = with(track) {
            AudioTrackUi(filePath, title, artist, duration, albumCover, sampleRate, mediaFormat, bitrate)
        }
    }
}