package com.github.astat1cc.vinylore.core.common_tracklist.data

import android.content.SharedPreferences
import androidx.core.net.toUri
import com.github.astat1cc.vinylore.core.AppConst
import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.common_tracklist.domain.CommonRepository
import com.github.astat1cc.vinylore.core.models.domain.AppAlbum
import kotlinx.coroutines.withContext

class CommonRepositoryImpl(
    private val sharedPrefs: SharedPreferences,
    private val fileProvider: AppFileProvider,
    private val dispatchers: DispatchersProvider
) : CommonRepository {

    override suspend fun fetchAlbums(): List<AppAlbum>? = withContext(dispatchers.io()) {
        val dirPath = sharedPrefs.getString(AppConst.CHOSEN_DIRECTORY_PATH_KEY, null)
            ?: return@withContext null
        return@withContext fileProvider.getAlbumListFrom(dirPath.toUri())
    }
}