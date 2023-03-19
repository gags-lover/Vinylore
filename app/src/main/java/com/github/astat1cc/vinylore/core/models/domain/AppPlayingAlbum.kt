package com.github.astat1cc.vinylore.core.models.domain

import android.net.Uri

data class AppPlayingAlbum(
    val folderPath: Uri,
    val name: String,
    val trackList: List<AppAudioTrack>,
    val albumOfOneArtist: Boolean
)