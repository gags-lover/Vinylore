package com.github.astat1cc.vinylore.albumlist.data

import android.content.SharedPreferences
import android.net.Uri
import com.github.astat1cc.vinylore.albumlist.domain.AlbumListScreenRepository
import com.github.astat1cc.vinylore.core.AppConst
import com.github.astat1cc.vinylore.player.data.MusicPlayerRepositoryImpl

class AlbumListScreenRepositoryImpl(
    private val sharedPrefs: SharedPreferences
) : AlbumListScreenRepository {

    override suspend fun saveChosenPlayingAlbumPath(albumUri: Uri) {
        sharedPrefs.edit()
            .putString(AppConst.PLAYING_ALBUM_PATH_KEY, albumUri.toString())
            .commit()
    }

    override suspend fun saveChosenDirectoryPath(dirPath: String) {
        sharedPrefs.edit()
            .putString(AppConst.CHOSEN_ROOT_DIRECTORY_PATH_KEY, dirPath)
            .apply()
    }
}