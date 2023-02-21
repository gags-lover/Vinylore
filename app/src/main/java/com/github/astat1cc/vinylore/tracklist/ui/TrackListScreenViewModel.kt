package com.github.astat1cc.vinylore.tracklist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.models.domain.ErrorType
import com.github.astat1cc.vinylore.core.models.ui.UiState
import com.github.astat1cc.vinylore.player.ui.service.MediaPlayerServiceConnection
import com.github.astat1cc.vinylore.tracklist.ui.model.TrackListScreenUiStateData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class TrackListScreenViewModel(
    private val errorHandler: AppErrorHandler,
    serviceConnection: MediaPlayerServiceConnection
) : ViewModel() {

    val uiState: StateFlow<UiState<TrackListScreenUiStateData>> =
        serviceConnection.playingAlbum.zip(serviceConnection.currentPlayingTrack) { album, currentTrack ->
            TrackListScreenUiStateData(album, currentTrack)
        }.map { uiStateData ->
            // to let animation be smooth without lagging
            delay(500L)
            when {
                uiStateData.album == null -> {
                    UiState.Fail(message = errorHandler.getErrorMessage(ErrorType.ALBUM_IS_NOT_CHOSEN))
                }
                uiStateData.album.trackList.isEmpty() -> {
                    UiState.Fail(message = errorHandler.getErrorMessage(ErrorType.DIR_IS_EMPTY))
                }
                else -> {
                    UiState.Success(uiStateData)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, UiState.Loading())

    val isPlaying = serviceConnection.isMusicPlaying // todo move in state
}