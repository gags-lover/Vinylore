package com.github.astat1cc.vinylore.tracklist.domain

import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.ErrorHandler
import com.github.astat1cc.vinylore.core.models.domain.FetchResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

interface TrackListInteractor {

    suspend fun saveChosenDirectoryPath(dirPath: String)

    fun fetchAlbums(): Flow<FetchResult>

    class Impl(
        private val repository: TrackListRepository,
        private val errorHandler: ErrorHandler,
        private val dispatchers: DispatchersProvider
    ) : TrackListInteractor {

        override suspend fun saveChosenDirectoryPath(dirPath: String) {
            withContext(dispatchers.io()) {
                repository.saveChosenDirectoryPath(dirPath)
            }
        }

        override fun fetchAlbums(): Flow<FetchResult> = flow {
            while (true) {
                try {
                    emit(
                        FetchResult.Success(albums = repository.fetchAlbums()) // todo maybe refresh just manually
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