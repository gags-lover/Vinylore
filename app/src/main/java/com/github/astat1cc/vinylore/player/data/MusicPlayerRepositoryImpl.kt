package com.github.astat1cc.vinylore.player.data

import android.content.SharedPreferences
import com.github.astat1cc.vinylore.core.AppConst
import com.github.astat1cc.vinylore.player.domain.MusicPlayerRepository

class MusicPlayerRepositoryImpl(
    private val sharedPrefs: SharedPreferences
) : MusicPlayerRepository {

    override suspend fun getLastPlayingAlbumPath(): String =
        sharedPrefs.getString(AppConst.PLAYING_ALBUM_PATH_KEY, "") ?: ""// todo handle if error
}