package com.github.astat1cc.vinylore.tracklist.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.ErrorHandler
import com.github.astat1cc.vinylore.tracklist.domain.TrackListInteractor
import com.github.astat1cc.vinylore.core.models.domain.FetchResult
import com.github.astat1cc.vinylore.core.models.ui.AlbumUi
import com.github.astat1cc.vinylore.tracklist.ui.models.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrackListViewModel(
    private val interactor: TrackListInteractor,
    private val dispatchers: DispatchersProvider,
    private val errorHandler: ErrorHandler
) : ViewModel() {

    val uiState: StateFlow<UiState> = interactor.fetchAlbums()
        .map { fetchResult -> fetchResult.toUiState() }
        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Loading)

    fun handleChosenDirUri(uri: Uri) = viewModelScope.launch(dispatchers.io()) {
        interactor.saveChosenDirectoryPath(uri.toString())
    }

    private fun FetchResult.toUiState(): UiState = when (this) {
        is FetchResult.Success -> {
            UiState.Success(
                albums?.map { albumDomain ->
                    AlbumUi.fromDomain(albumDomain)
                }
            )
        }
        is FetchResult.Fail -> {
            UiState.Fail(
                message = errorHandler.getErrorMessage(this.error)
            )
        }
    }
}
