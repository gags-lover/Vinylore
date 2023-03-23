package com.github.astat1cc.vinylore.albumlist.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.astat1cc.vinylore.SLIDE_IN_DURATION
import com.github.astat1cc.vinylore.albumlist.domain.AlbumListScreenInteractor
import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.models.domain.AppListingAlbum
import com.github.astat1cc.vinylore.core.models.domain.FetchResult
import com.github.astat1cc.vinylore.core.models.ui.ListingAlbumUi
import com.github.astat1cc.vinylore.core.models.ui.UiState
import com.github.astat1cc.vinylore.player.ui.service.MediaPlayerServiceConnection
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AlbumListScreenViewModel(
    private val interactor: AlbumListScreenInteractor,
    private val dispatchers: DispatchersProvider,
    private val errorHandler: AppErrorHandler,
    private val serviceConnection: MediaPlayerServiceConnection
) : ViewModel() {

//    val uiState: StateFlow<UiState<List<AlbumUi>?>> = interactor.fetchAlbums()
//        .map { fetchResult -> fetchResult.toUiState() }
//        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Loading())

    private val _uiState = MutableStateFlow<UiState<List<ListingAlbumUi>?>>(UiState.Loading())
    val uiState: StateFlow<UiState<List<ListingAlbumUi>?>> = _uiState.asStateFlow()

    private val _clickedAlbumUri = MutableStateFlow<Uri?>(null)
    val clickedAlbumUri: StateFlow<Uri?> = _clickedAlbumUri.asStateFlow()

    val shouldNavigateToPlayer = serviceConnection.shouldRefreshMusicService

    val currentPlayingAlbumUri: StateFlow<Uri?> = serviceConnection.playingAlbum.map { album ->
        album?.uri
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        enableAlbumsScan()
        viewModelScope.launch {
            // default delay makes transition animation smoother, because UiState.Loading wouldn't
            // be changed to UiState.Success with probably huge list of items
            val defaultDelay = launch { delay(SLIDE_IN_DURATION.toLong() + 100L) }
            interactor.fetchAlbums().collect { fetchResult ->
                defaultDelay.join()
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

    /**
     * Function returns true if saving succeed and false if the clicked album is already the
     * playing album.
     */
    suspend fun handleClickedAlbumUri(clickedAlbumUri: Uri): Boolean =
        viewModelScope.async(dispatchers.io()) {
            if (serviceConnection.playingAlbum.value?.uri == clickedAlbumUri) return@async false

            serviceConnection.clearCurrentPlayingTrack()

            interactor.saveChosenPlayingAlbum(clickedAlbumUri)
            _clickedAlbumUri.value = clickedAlbumUri
            delay(600L) // delay of album slideOut animation
            return@async true
        }.await()

    fun disableAlbumsScan() {
        interactor.disableAlbumsScan()
    }

    fun enableAlbumsScan() {
        viewModelScope.launch {
            // Popup doesn't hide itself immediately, so user can see how
            // 2 item popup turns into 1 item and then hiding. To escape that there should be delay
            // to give popup time to hide itself
            delay(40L)
            _uiState.value = UiState.Loading()
            interactor.enableAlbumsScan()
        }
    }

    private fun FetchResult<List<AppListingAlbum>?>.toUiState(): UiState<List<ListingAlbumUi>?> =
        when (this) {
            is FetchResult.Success -> {
                UiState.Success(
                    data?.map { albumDomain ->
                        ListingAlbumUi.fromDomain(albumDomain)
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