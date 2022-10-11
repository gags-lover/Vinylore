package com.github.astat1cc.vinylore.tracklist.domain

interface TrackListScreenRepository {

    suspend fun saveChosenDirectoryPath(dirPath: String)
}