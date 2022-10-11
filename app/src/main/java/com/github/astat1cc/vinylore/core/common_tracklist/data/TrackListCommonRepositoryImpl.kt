package com.github.astat1cc.vinylore.core.common_tracklist.data

import android.content.SharedPreferences
import androidx.core.net.toUri
import com.github.astat1cc.vinylore.core.AppConst
import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.common_tracklist.domain.TrackListCommonRepository
import com.github.astat1cc.vinylore.core.models.domain.AppAlbum
import com.github.astat1cc.vinylore.tracklist.data.AppFileProvider
import kotlinx.coroutines.withContext

class TrackListCommonRepositoryImpl(
    private val sharedPrefs: SharedPreferences,
    private val fileProvider: AppFileProvider,
    private val dispatchers: DispatchersProvider
) : TrackListCommonRepository {

    override suspend fun fetchAlbums(): List<AppAlbum>? = withContext(dispatchers.io()) {
        val dirPath = sharedPrefs.getString(AppConst.CHOSEN_DIRECTORY_PATH_KEY, null)
            ?: return@withContext null
        return@withContext fileProvider.getAlbumListFrom(dirPath.toUri())
    }
}