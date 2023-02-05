package com.github.astat1cc.vinylore.albumlist.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.astat1cc.vinylore.albumlist.domain.AlbumListScreenInteractor
import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.models.domain.AppAlbum
import com.github.astat1cc.vinylore.core.models.domain.FetchResult
import com.github.astat1cc.vinylore.core.models.ui.AlbumUi
import com.github.astat1cc.vinylore.core.models.ui.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AlbumListScreenViewModel(
    private val interactor: AlbumListScreenInteractor,
    private val dispatchers: DispatchersProvider,
    private val errorHandler: AppErrorHandler
) : ViewModel() {

//    val uiState: StateFlow<UiState<List<AlbumUi>?>> = interactor.fetchAlbums()
//        .map { fetchResult -> fetchResult.toUiState() }
//        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Loading())

    private val _uiState = MutableStateFlow<UiState<List<AlbumUi>?>>(UiState.Loading())
    val uiState: StateFlow<UiState<List<AlbumUi>?>> = _uiState.asStateFlow()

    init {
        enableAlbumsScan()
        viewModelScope.launch {
            interactor.fetchAlbums().collect { fetchResult ->
                _uiState.value = fetchResult.toUiState()
            }
        }
    }

//    init {
//        fetchAlbums()
//    }

//    fun fetchAlbums() {
//        viewModelScope.launch(dispatchers.io()) {
//            _uiState.value = interactor.fetchAlbums().toUiState()
//        }
//    }

    fun handleChosenDirUri(uri: Uri) = viewModelScope.launch(dispatchers.io()) {
        interactor.saveChosenDirectoryPath(uri.toString())
    }

    fun saveChosenPlayingAlbum(albumId: Int) = viewModelScope.launch(dispatchers.io()) {
        interactor.saveChosenPlayingAlbum(albumId)
    }

    fun disableAlbumsScan() {
        interactor.disableAlbumsScan()
    }

    fun enableAlbumsScan() {
        _uiState.value = UiState.Loading()
        interactor.enableAlbumsScan()
    }

    private fun FetchResult<List<AppAlbum>?>.toUiState(): UiState<List<AlbumUi>?> =
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