package com.github.astat1cc.vinylore.tracklist.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.models.domain.ErrorType
import com.github.astat1cc.vinylore.core.models.ui.AudioTrackUi
import com.github.astat1cc.vinylore.core.models.ui.UiState
import com.github.astat1cc.vinylore.player.ui.service.MediaPlayerServiceConnection
import com.github.astat1cc.vinylore.tracklist.ui.model.TrackListScreenUiStateData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

    fun skipToQueueItem(id: Long) {
        serviceConnection.skipToQueueItem(id)
    }
}