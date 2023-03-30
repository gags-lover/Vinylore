package com.github.astat1cc.vinylore.core.common_tracklist.data

import android.content.SharedPreferences
import android.util.Log
import androidx.core.net.toUri
import com.github.astat1cc.vinylore.core.AppConst
import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.common_tracklist.domain.CommonRepository
import com.github.astat1cc.vinylore.core.models.domain.AppListingAlbum
import com.github.astat1cc.vinylore.core.models.domain.AppPlayingAlbum
import kotlinx.coroutines.withContext

class CommonRepositoryImpl(
    private val sharedPrefs: SharedPreferences,
    private val fileProvider: AppFileProvider,
    private val dispatchers: DispatchersProvider
) : CommonRepository {

    private var albumListSnapshot: List<AppListingAlbum>? = null

    private var playingAlbumSnapshot: AppPlayingAlbum? = null

    override suspend fun fetchAlbumsForListing(): List<AppListingAlbum>? =
        // todo do i need refresh var now?
        withContext(dispatchers.io()) {
            initializeAlbums()
            albumListSnapshot // todo just return list with no var
        }

    override suspend fun fetchPlayingAlbum(refresh: Boolean): AppPlayingAlbum? =
        withContext(dispatchers.io()) {
            if (playingAlbumSnapshot == null || refresh) {
                initializePlayingAlbum()
                playingAlbumSnapshot
            } else {
                playingAlbumSnapshot
            }
        }

    private suspend fun initializeAlbums() {
        val dirPath = sharedPrefs.getString(AppConst.CHOSEN_ROOT_DIRECTORY_PATH_KEY, null)
        albumListSnapshot = dirPath?.let {
            fileProvider.getOnlyAlbumListFrom(dirPath.toUri())
        }
    }

    private suspend fun initializePlayingAlbum() {
        val albumPath = sharedPrefs.getString(AppConst.PLAYING_ALBUM_PATH_KEY, null)
        playingAlbumSnapshot = albumPath?.let {
            fileProvider.getAlbumFrom(albumPath.toUri())
        }
    }
}