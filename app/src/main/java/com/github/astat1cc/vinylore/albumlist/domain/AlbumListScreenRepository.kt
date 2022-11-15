package com.github.astat1cc.vinylore.albumlist.domain

interface AlbumListScreenRepository {

    suspend fun saveChosenDirectoryPath(dirPath: String)

    suspend fun saveChosenPlayingAlbumId(albumId: Int)
}