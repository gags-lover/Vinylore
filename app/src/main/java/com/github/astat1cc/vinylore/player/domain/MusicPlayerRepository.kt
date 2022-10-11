package com.github.astat1cc.vinylore.player.domain

interface MusicPlayerRepository {

    suspend fun saveLastPlayingAlbumId(albumId: Int)

    suspend fun getLastPlayingAlbumId(): Int
}