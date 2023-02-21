package com.github.astat1cc.vinylore.core.models.domain

import android.graphics.Bitmap

data class AppAudioTrack(
    val filePath: String,
    val name: String,// todo if i need nullable?
    val duration: Long,
    val albumCover: Bitmap?
)