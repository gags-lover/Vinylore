package com.github.astat1cc.vinylore.core.models.ui

import android.net.Uri
import androidx.core.net.toUri
import com.github.astat1cc.vinylore.core.models.domain.AppAudioTrack

data class AudioTrackUi(
    val uri: Uri,
    val name: String?
) {

    companion object {

        fun fromDomain(track: AppAudioTrack) = with(track) {
            AudioTrackUi(filePath.toUri(), name)
        }
    }
}