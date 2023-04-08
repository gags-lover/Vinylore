package com.github.astat1cc.vinylore.tracklist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.models.domain.ErrorType
import com.github.astat1cc.vinylore.core.models.ui.AudioTrackUi
import com.github.astat1cc.vinylore.core.models.ui.UiState
import com.github.astat1cc.vinylore.player.ui.service.CustomPlayerState
import com.github.astat1cc.vinylore.player.ui.service.MediaPlayerServiceConnection
import com.github.astat1cc.vinylore.tracklist.ui.model.TrackListScreenUiStateData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class TrackListScreenViewModel(
    private val errorHandler: AppErrorHandler,
    private val serviceConnection: MediaPlayerServiceConnection
) : ViewModel() {

    val uiState: StateFlow<UiState<TrackListScreenUiStateData>> =
        serviceConnection.playingAlbum.combine(serviceConnection.currentPlayingTrack) { album, currentTrack ->
            TrackListScreenUiStateData(album, currentTrack)
        }.map { uiStateData ->
            // to let animation be smooth without lagging
            delay(500L)
            when {
                // it means album is either not chosen or is empty
                uiStateData.album == null -> {
                    UiState.Fail(message = errorHandler.getErrorMessage(ErrorType.NoTrackList))
                }
                // never gets to it
                uiStateData.album.trackList.isEmpty() -> {
                    UiState.Fail(message = errorHandler.getErrorMessage(ErrorType.DirIsEmpty))
                }
                else -> {
                    UiState.Success(uiStateData)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, UiState.Loading())

    val shouldNavigateToPlayer = serviceConnection.shouldRefreshMusicService

    private val isMusicPlaying: StateFlow<Boolean> = serviceConnection.isMusicPlaying

    private val customPlayerState: StateFlow<CustomPlayerState> =
        serviceConnection.customPlayerState

    fun skipToQueueItem(id: Long, track: AudioTrackUi) {
        val state = uiState.value
        if (state is UiState.Success && state.data.currentPlayingTrack == track) {
            togglePlayPause()
            return
        }
        serviceConnection.skipToQueueItem(id)
    }

    private fun togglePlayPause() {
        if (customPlayerState.value == CustomPlayerState.IDLE) {
            // not slowly launching player to not to confuse user with delay with no player shown
            serviceConnection.transportControl.play()
        } else {
            if (isMusicPlaying.value) {
                serviceConnection.slowPause()
            } else {
                serviceConnection.slowResume()
            }
        }
    }
}