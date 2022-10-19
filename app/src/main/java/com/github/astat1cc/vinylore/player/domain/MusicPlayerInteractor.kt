package com.github.astat1cc.vinylore.player.domain

import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.common_tracklist.domain.TrackListCommonRepository
import com.github.astat1cc.vinylore.core.models.domain.AppAudioTrack
import com.github.astat1cc.vinylore.core.models.domain.FetchResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

interface MusicPlayerInteractor {

    suspend fun saveLastPlayingAlbum(albumId: Int)

    fun fetchTrackList(): Flow<FetchResult<List<AppAudioTrack>>>

    class Impl(
        private val playerRepository: MusicPlayerRepository,
        private val trackListRepository: TrackListCommonRepository,
        private val dispatchers: DispatchersProvider,
        private val errorHandler: AppErrorHandler
    ) : MusicPlayerInteractor {

        override suspend fun saveLastPlayingAlbum(albumId: Int) {
            withContext(dispatchers.io()) {
                playerRepository.saveLastPlayingAlbumId(albumId)
            }
        }

        override fun fetchTrackList(): Flow<FetchResult<List<AppAudioTrack>>> = flow {
            var prevResult: FetchResult<List<AppAudioTrack>>? = null
            while (true) {
                try {
                    val albumIdToFetch = playerRepository.getLastPlayingAlbumId()
                    val trackList = trackListRepository.fetchAlbums()?.find { album ->
                        album.id == albumIdToFetch
                    }?.trackList
                    val newResult = FetchResult.Success(data = trackList)
                    if (prevResult != newResult) {
                        prevResult = newResult
                        emit(
                            prevResult // todo maybe refresh just manually
                        )
                    }

                } catch (e: Exception) {
                    val newResult =
                        FetchResult.Fail<List<AppAudioTrack>>(error = errorHandler.getErrorTypeOf(e))
                    if (prevResult != newResult) {
                        prevResult = newResult
                        emit(
                            prevResult // todo maybe refresh just manually
                        )
                    }
                }
                delay(750L) // todo maybe finally?
            }
        }
    }
}