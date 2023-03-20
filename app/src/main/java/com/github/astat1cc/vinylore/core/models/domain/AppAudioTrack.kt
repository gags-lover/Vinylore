package com.github.astat1cc.vinylore.core.models.domain

import android.graphics.Bitmap
import android.net.Uri

data class AppAudioTrack(
    val filePath: Uri,
    val title: String,
    val duration: Long,
    val albumCover: Bitmap?,
    val artist: String?,
    val fileName: String,
    val genre: String?,
    val year: String?,
    val bitrate: String?
)