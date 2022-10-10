package com.github.astat1cc.vinylore.tracklist.domain

import com.github.astat1cc.vinylore.core.models.domain.AppAlbum

interface TrackListRepository {

    suspend fun saveChosenDirectoryPath(dirPath: String)

    suspend fun fetchAlbums(): List<AppAlbum>?
}