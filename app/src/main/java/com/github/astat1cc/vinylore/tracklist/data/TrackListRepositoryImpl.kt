package com.github.astat1cc.vinylore.tracklist.data

import android.content.SharedPreferences
import androidx.core.net.toUri
import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.models.domain.AppAlbum
import com.github.astat1cc.vinylore.tracklist.data.dao.TrackListDao
import com.github.astat1cc.vinylore.tracklist.domain.TrackListRepository
import com.github.astat1cc.vinylore.tracklist.ui.AppFileProvider
import kotlinx.coroutines.withContext

class TrackListRepositoryImpl(
    private val sharedPrefs: SharedPreferences,
    private val fileProvider: AppFileProvider,
    private val dispatchers: DispatchersProvider,
    private val dao: TrackListDao
) : TrackListRepository {

    override suspend fun saveChosenDirectoryPath(dirPath: String) {
        sharedPrefs.edit().putString(CHOSEN_DIRECTORY_PATH_KEY, dirPath).apply()
    }

    override suspend fun fetchAlbums(): List<AppAlbum>? = withContext(dispatchers.io()) {
        val dirPath =
            sharedPrefs.getString(CHOSEN_DIRECTORY_PATH_KEY, null) ?: return@withContext null
        return@withContext fileProvider.getAlbumListFrom(dirPath.toUri())
    }

    companion object {

        const val CHOSEN_DIRECTORY_PATH_KEY = "CHOSEN_DIRECTORY_PATH_KEY"
    }
}