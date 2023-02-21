package com.github.astat1cc.vinylore.core.models.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import com.github.astat1cc.vinylore.core.models.domain.AppAudioTrack

data class AudioTrackUi(
    val uri: Uri,
    val name: String?,
    val duration: Long,
    val albumCover: Bitmap?
) {

    companion object {

        fun fromDomain(track: AppAudioTrack) = with(track) {
            AudioTrackUi(filePath.toUri(), name, duration, albumCover)
        }
    }
}