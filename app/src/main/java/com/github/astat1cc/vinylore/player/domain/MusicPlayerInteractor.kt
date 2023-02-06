package com.github.astat1cc.vinylore.player.domain

import android.util.Log
import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.common_tracklist.domain.CommonRepository
import com.github.astat1cc.vinylore.core.models.domain.AppAlbum
import com.github.astat1cc.vinylore.core.models.domain.FetchResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

interface MusicPlayerInteractor {

//    fun fetchAlbum(): Flow<FetchResult<AppAlbum?>>

//    suspend fun startAlbumCheckingLoop()

//    fun getFlow(): SharedFlow<FetchResult<AppAlbum?>>

    /**
     * Always returns the same instance of SharedFlow
     */
    fun getAlbumFlow(): SharedFlow<FetchResult<AppAlbum?>>

    /**
     * Fetching album and emitting FetchResult to the instance of SharedFlow, which can be returned
     * by calling getAlbumFlow(). Originally this instance of SharedFlow is not initialized
     * and staying empty if you don't call this function.
     */
    suspend fun initializeAlbum()

    class Impl(
        private val playerRepository: MusicPlayerRepository,
        private val commonRepository: CommonRepository,
        private val dispatchers: DispatchersProvider,
        private val errorHandler: AppErrorHandler
    ) : MusicPlayerInteractor {

        // we need the same instance for viewmodel and service, so we're holding our album
        // in a variable instead of creating and returning new flow every time the function called
        private val playingAlbum = MutableSharedFlow<FetchResult<AppAlbum?>>(replay = 1)

        override fun getAlbumFlow(): SharedFlow<FetchResult<AppAlbum?>> =
            playingAlbum.asSharedFlow()

        override suspend fun initializeAlbum() {
            withContext(dispatchers.io()) {
                try {
                    val albumIdToFetch = playerRepository.getLastPlayingAlbumId()
                    val albumFound: AppAlbum? = commonRepository.fetchAlbums()?.find { album ->
                        album.id == albumIdToFetch
                    }
                    playingAlbum.emit(
                        FetchResult.Success(data = albumFound)
                    )
                } catch (e: Exception) {
                    playingAlbum.emit(
                        FetchResult.Fail(error = errorHandler.getErrorTypeOf(e))
                    )
                }
            }

            // called by viewModel
//        override suspend fun startAlbumCheckingLoop() {
//            withContext(dispatchers.io()) {
//                var prevResult: FetchResult<AppAlbum?>? = null
//                while (true) {
//                    try {
//                        val albumIdToFetch = playerRepository.getLastPlayingAlbumId()
//                        val albumFound = commonRepository.fetchAlbums()?.find { album ->
//                            album.id == albumIdToFetch
//                        }
//                        val newResult = FetchResult.Success(data = albumFound)
//                        if (prevResult != newResult) {
//                            prevResult = newResult
//                            playingAlbum.emit(prevResult)
//                        }
//                    } catch (e: Exception) {
//                        val newResult =
//                            FetchResult.Fail<AppAlbum?>(error = errorHandler.getErrorTypeOf(e))
//                        if (prevResult != newResult) {
//                            prevResult = newResult
//                            playingAlbum.emit(prevResult)
//                        }
//                    }
//                    delay(2000L)
//                }
//            }
//        }

            // called by viewmodel and mediaSource
//        override fun getFlow(): SharedFlow<FetchResult<AppAlbum?>> = playingAlbum.asSharedFlow()

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
//                        emit(prevResult)
//                    }
//                } catch (e: Exception) {
//                    val newResult =
//                        FetchResult.Fail<AppAlbum?>(error = errorHandler.getErrorTypeOf(e))
//                    if (prevResult != newResult) {
//                        prevResult = newResult
//                        emit(prevResult)
//                    }
//                }
//                delay(2750L)
//            }
//        }
        }
    }
}