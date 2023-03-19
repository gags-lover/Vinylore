package com.github.astat1cc.vinylore.core.models.domain

import android.net.Uri

data class AppListingAlbum(
    val folderPath: Uri,
    val name: String,
    val trackCount: Int
)