package com.github.astat1cc.vinylore.tracklist.data

import android.content.SharedPreferences
import com.github.astat1cc.vinylore.core.AppConst
import com.github.astat1cc.vinylore.tracklist.domain.TrackListScreenRepository

class TrackListScreenRepositoryImpl(
    private val sharedPrefs: SharedPreferences
) : TrackListScreenRepository {

    override suspend fun saveChosenDirectoryPath(dirPath: String) {
        sharedPrefs.edit()
            .putString(AppConst.CHOSEN_DIRECTORY_PATH_KEY, dirPath)
            .apply()
    }
}