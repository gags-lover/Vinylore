package com.github.astat1cc.vinylore.albumlist.domain

import android.net.Uri
import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.models.domain.AppListingAlbum
import com.github.astat1cc.vinylore.core.models.domain.FetchResult
import kotlinx.coroutines.withContext

interface AlbumListScreenInteractor {

    suspend fun saveChosenDirectoryPath(dirPath: String)

    suspend fun saveChosenPlayingAlbum(albumUri: Uri)

    suspend fun fetchLastChosenAlbum(): Uri?

    suspend fun fetchAlbums(scanFirst: Boolean): FetchResult<List<AppListingAlbum>?>

    suspend fun saveAlbumsInDatabase()

//    fun disableAlbumsScan()

//    fun enableAlbumsScan()

    class Impl(
        private val dispatchers: DispatchersProvider,
        private val albumListScreenRepository: AlbumListScreenRepository,
        private val errorHandler: AppErrorHandler
    ) : AlbumListScreenInteractor {

//        private var needToCheckAlbumList = true

        override suspend fun saveChosenDirectoryPath(dirPath: String) {
            withContext(dispatchers.io()) {
                albumListScreenRepository.saveChosenDirectoryPath(dirPath)
            }
        }

        override suspend fun saveChosenPlayingAlbum(albumUri: Uri) {
            withContext(dispatchers.io()) {
                albumListScreenRepository.saveChosenPlayingAlbumPath(albumUri)
            }
        }

        override suspend fun fetchLastChosenAlbum(): Uri? =
            withContext(dispatchers.io()) {
                albumListScreenRepository.fetchLastChosenAlbum()
            }

        /**
         * Function returns null if it's first app launch, i.e. there's no saved albums and no
         * chosen directory to scan from.
         */
        override suspend fun fetchAlbums(scanFirst: Boolean): FetchResult<List<AppListingAlbum>?> =
            withContext(dispatchers.io()) {
                try {
                    FetchResult.Success(
                        data = albumListScreenRepository.fetchAlbumsForListing(
                            scanFirst
                        )
                    )
                } catch (e: Exception) {
                    FetchResult.Fail(error = errorHandler.getErrorTypeOf(e))
                }
            }

        override suspend fun saveAlbumsInDatabase() {
            withContext(dispatchers.io()) {
                albumListScreenRepository.saveAlbumsInDatabase()
            }
        }

//        override fun fetchAlbums(): Flow<FetchResult<List<AppListingAlbum>?>> = flow {
//            while (true) {
//                if (needToCheckAlbumList) {
//                    try {
//                        emit(
//                            FetchResult.Success(data = commonRepository.fetchAlbumsForListing())
//                        )
//                    } catch (e: Exception) {
////                        throw e // todo
//                        emit(
//                            FetchResult.Fail(error = errorHandler.getErrorTypeOf(e))
//                        )
//                    }
//                }
//                delay(750L)
//            }
//        }

//        override fun disableAlbumsScan() {
//            needToCheckAlbumList = false
//        }

//        override fun enableAlbumsScan() {
//            needToCheckAlbumList = true
//        }
    }
}