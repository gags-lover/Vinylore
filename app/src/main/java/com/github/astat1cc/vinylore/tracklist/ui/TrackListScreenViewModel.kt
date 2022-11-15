package com.github.astat1cc.vinylore.tracklist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.models.domain.ErrorType
import com.github.astat1cc.vinylore.core.models.ui.AlbumUi
import com.github.astat1cc.vinylore.core.models.ui.UiState
import com.github.astat1cc.vinylore.player.ui.service.MediaPlayerServiceConnection
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TrackListScreenViewModel(
    private val errorHandler: AppErrorHandler,
    serviceConnection: MediaPlayerServiceConnection
) : ViewModel() {

    val uiState: StateFlow<UiState<AlbumUi>> =
        serviceConnection.playingAlbum.map { album ->
            when {
                album == null -> {
                    UiState.Fail(message = errorHandler.getErrorMessage(ErrorType.ALBUM_IS_NOT_CHOSEN))
                }
                album.trackList.isEmpty() -> {
                    UiState.Fail(message = errorHandler.getErrorMessage(ErrorType.DIR_IS_EMPTY))
                }
                else -> {
                    UiState.Success(album)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, UiState.Loading())
}