package com.github.astat1cc.vinylore.albumlist.domain

import android.net.Uri

interface AlbumListScreenRepository {

    suspend fun saveChosenDirectoryPath(dirPath: String)

    suspend fun saveChosenPlayingAlbumPath(albumUri: Uri)
}