package com.github.astat1cc.vinylore.player.domain

import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.common_tracklist.domain.CommonRepository
import com.github.astat1cc.vinylore.core.models.domain.AppAlbum
import com.github.astat1cc.vinylore.core.models.domain.AppAudioTrack
import com.github.astat1cc.vinylore.core.models.domain.FetchResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

interface MusicPlayerInteractor {

    fun fetchTrackList(): Flow<FetchResult<AppAlbum?>>

    class Impl(
        private val playerRepository: MusicPlayerRepository,
        private val commonRepository: CommonRepository,
        private val dispatchers: DispatchersProvider, // todo maybe remove?
        private val errorHandler: AppErrorHandler
    ) : MusicPlayerInteractor {

        override fun fetchTrackList(): Flow<FetchResult<AppAlbum?>> = flow {
            var prevResult: FetchResult<AppAlbum?>? = null
            while (true) {
                try {
                    val albumIdToFetch = playerRepository.getLastPlayingAlbumId()
                    val albumFound = commonRepository.fetchAlbums()?.find { album ->
                        album.id == albumIdToFetch
                    }
                    val newResult = FetchResult.Success(data = albumFound)
                    if (prevResult == newResult) continue
                    prevResult = newResult
                    emit(prevResult) // todo maybe refresh just manually
                } catch (e: Exception) {
                    val newResult =
                        FetchResult.Fail<AppAlbum?>(error = errorHandler.getErrorTypeOf(e))
                    if (prevResult != newResult) continue
                    prevResult = newResult
                    emit(prevResult) // todo maybe refresh just manually
                }
                delay(750L) // todo maybe finally?
            }
        }
    }
}