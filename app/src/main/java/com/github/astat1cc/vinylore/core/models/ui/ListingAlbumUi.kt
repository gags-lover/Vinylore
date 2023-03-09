package com.github.astat1cc.vinylore.core.models.ui

import android.net.Uri
import androidx.core.net.toUri
import com.github.astat1cc.vinylore.core.models.domain.AppListingAlbum

data class ListingAlbumUi(
    val uri: Uri,
    val name: String?
) {

    companion object {

        fun fromDomain(album: AppListingAlbum) = with(album) {
            ListingAlbumUi(
                folderPath.toUri(),
                name
            )
        }
    }
}