package com.github.astat1cc.vinylore.player.domain

import android.util.Log
import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.common_tracklist.domain.CommonRepository
import com.github.astat1cc.vinylore.core.models.domain.AppPlayingAlbum
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
    fun getAlbumFlow(): SharedFlow<FetchResult<AppPlayingAlbum?>?>

    /**
     * Fetching album and emitting FetchResult to the instance of SharedFlow, which can be returned
     * by calling getAlbumFlow(). Originally this instance of SharedFlow is not initialized
     * and staying empty if you don't call this function.
     */
    suspend fun initializeAlbum()

//    fun clearFlow()

    class Impl(
//        private val playerRepository: MusicPlayerRepository,
        private val commonRepository: CommonRepository,
        private val dispatchers: DispatchersProvider,
        private val errorHandler: AppErrorHandler
    ) : MusicPlayerInteractor {

        // we need the same instance for viewmodel and service, so we're holding our album
        // in a variable instead of creating and returning new flow every time the function called
        private val playingAlbum = MutableSharedFlow<FetchResult<AppPlayingAlbum?>?>(replay = 1)

        override fun getAlbumFlow(): SharedFlow<FetchResult<AppPlayingAlbum?>?> =
            playingAlbum.asSharedFlow()

        override suspend fun initializeAlbum() {
            playingAlbum.emit(null)
            withContext(dispatchers.io()) {
                try {
                    playingAlbum.emit(
                        FetchResult.Success(data = commonRepository.fetchPlayingAlbum(refresh = true))
                    )
                } catch (e: Exception) {
                    playingAlbum.emit(
                        FetchResult.Fail(error = errorHandler.getErrorTypeOf(e))
                    )
                }
            }
        }
    }
}