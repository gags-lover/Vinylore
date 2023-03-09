package com.github.astat1cc.vinylore.core.models.domain

data class AppPlayingAlbum(
    val folderPath: String,
    val name: String,
    val trackList: List<AppAudioTrack>,
    val albumOfOneArtist: Boolean
)