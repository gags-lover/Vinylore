package com.github.astat1cc.vinylore.albumlist.domain

import android.net.Uri
import com.github.astat1cc.vinylore.core.models.domain.AppListingAlbum

interface AlbumListScreenRepository {

    suspend fun saveChosenDirectoryPath(dirPath: String)

    suspend fun saveChosenPlayingAlbumPath(albumUri: Uri)

    suspend fun fetchAlbumsForListing(scanFirst: Boolean): List<AppListingAlbum>?

    suspend fun saveAlbumsInDatabase()

    suspend fun fetchLastChosenAlbum(): Uri?

    suspend fun fetchLastScanTimeAgo(): String?
}