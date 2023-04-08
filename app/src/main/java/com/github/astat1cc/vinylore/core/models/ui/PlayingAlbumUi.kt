package com.github.astat1cc.vinylore.core.models.ui

import android.net.Uri
import androidx.core.net.toUri
import com.github.astat1cc.vinylore.core.models.domain.AppPlayingAlbum

data class PlayingAlbumUi(
    val uri: Uri,
    val name: String,
    val trackList: List<AudioTrackUi>,
    val albumOfOneArtist: Boolean
) {

    companion object {

        fun fromDomain(album: AppPlayingAlbum) = with(album) {
            PlayingAlbumUi(
                folderPath,
                name,
                trackList.map { albumDomain -> AudioTrackUi.fromDomain(albumDomain) },
                albumOfOneArtist
            )
        }
    }
}