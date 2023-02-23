package com.github.astat1cc.vinylore.albumlist.domain

import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.common_tracklist.domain.CommonRepository
import com.github.astat1cc.vinylore.core.models.domain.AppAlbum
import com.github.astat1cc.vinylore.core.models.domain.FetchResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

interface AlbumListScreenInteractor {

    suspend fun saveChosenDirectoryPath(dirPath: String)

    suspend fun saveChosenPlayingAlbum(albumId: Int)

    fun fetchAlbums(refresh: Boolean): Flow<FetchResult<List<AppAlbum>?>>

    fun disableAlbumsScan()

    fun enableAlbumsScan()

    class Impl(
        private val dispatchers: DispatchersProvider,
        private val albumListScreenRepository: AlbumListScreenRepository,
        private val commonRepository: CommonRepository,
        private val errorHandler: AppErrorHandler
    ) : AlbumListScreenInteractor {

        private var needToCheckAlbumList = true

        override suspend fun saveChosenDirectoryPath(dirPath: String) {
            withContext(dispatchers.io()) {
                albumListScreenRepository.saveChosenDirectoryPath(dirPath)
            }
        }

        override suspend fun saveChosenPlayingAlbum(albumId: Int) {
            withContext(dispatchers.io()) {
                albumListScreenRepository.saveChosenPlayingAlbumId(albumId)
            }
        }

//        override suspend fun fetchAlbums(): FetchResult<List<AppAlbum>?> =
//            try {
//                FetchResult.Success(data = commonRepository.fetchAlbums())
//            } catch (e: Exception) {
//                FetchResult.Fail(error = errorHandler.getErrorTypeOf(e))
//            }

        override fun fetchAlbums(refresh: Boolean): Flow<FetchResult<List<AppAlbum>?>> = flow {
            while (true) {
                if (needToCheckAlbumList) {
                    try {
                        emit(
                            FetchResult.Success(data = commonRepository.fetchAlbums(refresh))
                        )
                    } catch (e: Exception) {
                        throw e // todo
                        emit(
                            FetchResult.Fail(error = errorHandler.getErrorTypeOf(e))
                        )
                    }
                }
                delay(750L)
            }
        }

        override fun disableAlbumsScan() {
            needToCheckAlbumList = false
        }

        override fun enableAlbumsScan() {
            needToCheckAlbumList = true
        }
    }
}