package com.github.astat1cc.vinylore.player.domain

import android.util.Log
import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.common_tracklist.domain.CommonRepository
import com.github.astat1cc.vinylore.core.models.domain.AppAlbum
import com.github.astat1cc.vinylore.core.models.domain.FetchResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

interface MusicPlayerInteractor {

//    fun fetchAlbum(): Flow<FetchResult<AppAlbum?>>

    suspend fun startAlbumCheckingLoop()

    fun getFlow(): SharedFlow<FetchResult<AppAlbum?>>

    class Impl(
        private val playerRepository: MusicPlayerRepository,
        private val commonRepository: CommonRepository,
        private val dispatchers: DispatchersProvider, // todo maybe remove?
        private val errorHandler: AppErrorHandler
    ) : MusicPlayerInteractor {

        private val playingAlbum = MutableSharedFlow<FetchResult<AppAlbum?>>(replay = 1)

        // called by viewModel
        override suspend fun startAlbumCheckingLoop() {
            withContext(dispatchers.io()) {
                var prevResult: FetchResult<AppAlbum?>? = null
                while (true) {
                    try {
                        val albumIdToFetch = playerRepository.getLastPlayingAlbumId()
                        val albumFound = commonRepository.fetchAlbums()?.find { album ->
                            album.id == albumIdToFetch
                        }
                        val newResult = FetchResult.Success(data = albumFound)
                        if (prevResult != newResult) {
                            prevResult = newResult
                            playingAlbum.emit(prevResult) // todo maybe refresh just manually
                        }
                    } catch (e: Exception) {
                        val newResult =
                            FetchResult.Fail<AppAlbum?>(error = errorHandler.getErrorTypeOf(e))
                        if (prevResult != newResult) {
                            prevResult = newResult
                            playingAlbum.emit(prevResult) // todo maybe refresh just manually
                        }
                    }
                    delay(2000L) // todo maybe finally?
                }
            }
        }

        // called by viewmodel and mediaSource
        override fun getFlow(): SharedFlow<FetchResult<AppAlbum?>> = playingAlbum

//        override fun fetchAlbum(): Flow<FetchResult<AppAlbum?>> = flow {
//            var prevResult: FetchResult<AppAlbum?>? = null
//            while (true) {
//                try {
//                    val albumIdToFetch = playerRepository.getLastPlayingAlbumId()
//                    val albumFound = commonRepository.fetchAlbums()?.find { album ->
//                        album.id == albumIdToFetch
//                    }
//                    val newResult = FetchResult.Success(data = albumFound)
////                    Log.e("album", "$albumFound")
//                    if (prevResult != newResult) {
//                        prevResult = newResult
//                        emit(prevResult) // todo maybe refresh just manually
//                    }
//                } catch (e: Exception) {
//                    val newResult =
//                        FetchResult.Fail<AppAlbum?>(error = errorHandler.getErrorTypeOf(e))
//                    if (prevResult != newResult) {
//                        prevResult = newResult
//                        emit(prevResult) // todo maybe refresh just manually
//                    }
//                }
//                delay(2750L) // todo maybe finally?
//            }
//        }
    }
}