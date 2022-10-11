package com.github.astat1cc.vinylore.tracklist.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.models.domain.AppAlbum
import com.github.astat1cc.vinylore.core.models.domain.FetchResult
import com.github.astat1cc.vinylore.core.models.ui.AlbumUi
import com.github.astat1cc.vinylore.tracklist.domain.TrackListScreenInteractor
import com.github.astat1cc.vinylore.tracklist.ui.models.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrackListScreenViewModel(
    private val interactor: TrackListScreenInteractor,
    private val dispatchers: DispatchersProvider,
    private val errorHandler: AppErrorHandler
) : ViewModel() {

    val uiState: StateFlow<UiState<List<AlbumUi>>> = interactor.fetchAlbums()
        .map { fetchResult -> fetchResult.toUiState() }
        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Loading())

    fun handleChosenDirUri(uri: Uri) = viewModelScope.launch(dispatchers.io()) {
        interactor.saveChosenDirectoryPath(uri.toString())
    }

    private fun FetchResult<List<AppAlbum>>.toUiState(): UiState<List<AlbumUi>> =
        when (this) {
            is FetchResult.Success -> {
                UiState.Success(
                    data?.map { albumDomain ->
                        AlbumUi.fromDomain(albumDomain)
                    }
                )
            }
            is FetchResult.Fail -> {
                UiState.Fail(
                    message = errorHandler.getErrorMessage(error)
                )
            }
        }
}