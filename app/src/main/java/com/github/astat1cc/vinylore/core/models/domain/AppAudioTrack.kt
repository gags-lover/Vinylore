package com.github.astat1cc.vinylore.core.models.domain

import android.graphics.Bitmap

data class AppAudioTrack(
    val filePath: String,
    val title: String,
    val duration: Long,
    val albumCover: Bitmap?,
    val artist: String?,
)