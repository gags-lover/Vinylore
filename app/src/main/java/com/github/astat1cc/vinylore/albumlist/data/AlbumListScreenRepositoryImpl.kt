package com.github.astat1cc.vinylore.albumlist.data

import android.content.SharedPreferences
import com.github.astat1cc.vinylore.albumlist.domain.AlbumListScreenRepository
import com.github.astat1cc.vinylore.core.AppConst
import com.github.astat1cc.vinylore.player.data.MusicPlayerRepositoryImpl

class AlbumListScreenRepositoryImpl(
    private val sharedPrefs: SharedPreferences
) : AlbumListScreenRepository {

    override suspend fun saveChosenPlayingAlbumId(albumId: Int) {
        sharedPrefs.edit()
            .putInt(MusicPlayerRepositoryImpl.LAST_PLAYING_ALBUM_ID_KEY, albumId)
            .apply()
    }

    override suspend fun saveChosenDirectoryPath(dirPath: String) {
        sharedPrefs.edit()
            .putString(AppConst.CHOSEN_DIRECTORY_PATH_KEY, dirPath)
            .apply()
    }
}