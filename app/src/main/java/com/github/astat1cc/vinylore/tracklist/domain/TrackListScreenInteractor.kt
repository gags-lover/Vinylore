package com.github.astat1cc.vinylore.tracklist.domain

import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.common_tracklist.domain.TrackListCommonRepository
import com.github.astat1cc.vinylore.core.models.domain.AppAlbum
import com.github.astat1cc.vinylore.core.models.domain.FetchResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

interface TrackListScreenInteractor {

    suspend fun saveChosenDirectoryPath(dirPath: String)

    fun fetchAlbums(): Flow<FetchResult<List<AppAlbum>>>

    class Impl(
        private val dispatchers: DispatchersProvider,
        private val trackListScreenRepository: TrackListScreenRepository,
        private val trackListCommonRepository: TrackListCommonRepository,
        private val errorHandler: AppErrorHandler
    ) : TrackListScreenInteractor {

        override suspend fun saveChosenDirectoryPath(dirPath: String) {
            withContext(dispatchers.io()) {
                trackListScreenRepository.saveChosenDirectoryPath(dirPath)
            }
        }

        override fun fetchAlbums(): Flow<FetchResult<List<AppAlbum>>> = flow {
            while (true) {
                try {
                    emit(
                        FetchResult.Success(data = trackListCommonRepository.fetchAlbums()) // todo maybe refresh just manually
                    )
                } catch (e: Exception) {
                    emit(
                        FetchResult.Fail(error = errorHandler.getErrorTypeOf(e))
                    )
                }
                delay(750L) // todo maybe finally?}
            }
        }
    }
}