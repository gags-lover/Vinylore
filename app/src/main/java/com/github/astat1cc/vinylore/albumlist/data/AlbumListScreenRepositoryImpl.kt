package com.github.astat1cc.vinylore.albumlist.data

import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.github.astat1cc.vinylore.albumlist.data.database.dao.ListingAlbumDao
import com.github.astat1cc.vinylore.albumlist.data.database.model.ListingAlbumDb
import com.github.astat1cc.vinylore.albumlist.domain.AlbumListScreenRepository
import com.github.astat1cc.vinylore.core.AppConst
import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.common_tracklist.data.AppFileProvider
import com.github.astat1cc.vinylore.core.models.domain.AppListingAlbum
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class AlbumListScreenRepositoryImpl(
    private val sharedPrefs: SharedPreferences,
    private val dispatchers: DispatchersProvider,
    private val fileProvider: AppFileProvider,
    private val albumsDao: ListingAlbumDao
) : AlbumListScreenRepository {

    /**
     * Initially equals to null and being scanned, if it changes value to not empty list, that list
     * would be saved in database, else scanning this variable ends.
     */
    private var albumsToSave: List<AppListingAlbum>? = null

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

    override suspend fun fetchAlbumsForListing(scanFirst: Boolean): List<AppListingAlbum>? =
        withContext(dispatchers.io()) {
            if (scanFirst) {
                Log.e("database", "fetch from file provider")
                return@withContext fetchAlbumsFromFileProvider()
            }
            val albumsFromDb = fetchAlbumsFromDatabase()
            return@withContext if (albumsFromDb.isEmpty()) {
                Log.e("database", "fetch from file provider")
                fetchAlbumsFromFileProvider()
            } else {
                Log.e("database", "fetch from database")
                albumsToSave = emptyList() // it means albums saving is not needed
                albumsFromDb
            }
        }

    private suspend fun fetchAlbumsFromFileProvider(): List<AppListingAlbum>? =
        sharedPrefs.getString(AppConst.CHOSEN_ROOT_DIRECTORY_PATH_KEY, null)?.let { dirPath ->
            val albums = fileProvider.getOnlyAlbumListFrom(dirPath.toUri())
            albumsToSave = albums
            Log.e("database", "fetched from file provider count: ${albums.size}")
            return@let albums
        }

    private suspend fun fetchAlbumsFromDatabase(): List<AppListingAlbum> =
        albumsDao.fetchAlbums().map { albumDb -> albumDb.toDomain() }


    override suspend fun saveAlbumsInDatabase() {
        try {
            while (true) {
                if (albumsToSave != null) {
                    if (albumsToSave!!.isNotEmpty()) {
                        saveAlbumsInDatabase(albumsToSave)
                    }
                    return
                }
                delay(1000L)
            }
        } catch (e: Exception) {
            Log.e("exceptions", "exception in saveAlbumsIfThereWasScanning")
        }
    }

    private suspend fun saveAlbumsInDatabase(albums: List<AppListingAlbum>?) {
        if (albums == null) return
        albumsDao.clearAlbums()
        albumsDao.saveAlbums(albums.map { album -> ListingAlbumDb.fromDomain(album) })
        Log.e("database", "albums saved $albums")
    }
}