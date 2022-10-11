package com.github.astat1cc.vinylore.player.data

import android.content.SharedPreferences
import com.github.astat1cc.vinylore.player.domain.MusicPlayerRepository

class MusicPlayerRepositoryImpl(
    private val sharedPrefs: SharedPreferences
) : MusicPlayerRepository {

    override suspend fun saveLastPlayingAlbumId(albumId: Int) {
        sharedPrefs.edit().putInt(LAST_PLAYING_ALBUM_ID_KEY, albumId).apply()
    }

    override suspend fun getLastPlayingAlbumId(): Int =
        sharedPrefs.getInt(LAST_PLAYING_ALBUM_ID_KEY, -1) // todo handle if -1

    companion object {

        const val LAST_PLAYING_ALBUM_ID_KEY = "LAST_PLAYING_ALBUM_ID_KEY"
    }
}