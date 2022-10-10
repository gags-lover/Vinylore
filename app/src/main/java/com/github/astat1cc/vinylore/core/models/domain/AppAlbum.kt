package com.github.astat1cc.vinylore.core.models.domain

data class AppAlbum(
    val id: Int,
    val name: String,
    val trackList: List<AppAudioTrack>
)