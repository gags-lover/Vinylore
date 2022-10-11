package com.github.astat1cc.vinylore.player.ui

import android.support.v4.media.MediaBrowserCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.astat1cc.vinylore.Consts
import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.models.domain.AppAudioTrack
import com.github.astat1cc.vinylore.core.models.domain.FetchResult
import com.github.astat1cc.vinylore.player.ui.service.MediaPlayerServiceConnection
import com.github.astat1cc.vinylore.player.ui.service.MusicService
import com.github.astat1cc.vinylore.player.ui.service.currentPosition
import com.github.astat1cc.vinylore.player.ui.service.isPlaying
import com.github.astat1cc.vinylore.core.models.ui.AudioTrackUi
import com.github.astat1cc.vinylore.player.domain.MusicPlayerInteractor
import com.github.astat1cc.vinylore.player.ui.vinyl.PlayerState
import com.github.astat1cc.vinylore.tracklist.ui.models.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AudioViewModel(
    private val interactor: MusicPlayerInteractor,
    private val errorHandler: AppErrorHandler,
    serviceConnection: MediaPlayerServiceConnection
) : ViewModel() {

    val uiState: StateFlow<UiState<List<AudioTrackUi>>> =
        interactor.fetchTrackList()
            .map { fetchResult -> fetchResult.toUiState() }
            .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Loading())

    var currentPlayingAudio = serviceConnection.currentPlayingAudio
    private val isConnected = serviceConnection.isConnected
    lateinit var rootMediaId: String
    var currentPlaybackPosition by mutableStateOf(0L)
    private var updatePosition = true
    private val playbackState =
        serviceConnection.playbackState // todo make flow of only needed variables

    private val isPlaying: StateFlow<Boolean> = playbackState.map { state ->
        state?.isPlaying == true
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _playerAnimationState = MutableStateFlow(PlayerState.STOPPED)
    val playerAnimationState = _playerAnimationState.asStateFlow()

    private val shouldShowSmoothStartAndStopVynilAnimation = MutableStateFlow(true)

    init {
        viewModelScope.launch {
            _playerAnimationState.emitAll(
                isPlaying.map { isPlaying ->
                    if (isPlaying) {
                        if (shouldShowSmoothStartAndStopVynilAnimation.value) {
                            PlayerState.STARTING
                        } else PlayerState.STARTED
                    } else {
                        if (shouldShowSmoothStartAndStopVynilAnimation.value) {
                            PlayerState.STOPPING
                        } else PlayerState.STOPPED
                    }
                }
            )
        }
        viewModelScope.launch {
            isConnected.collect { isConnected ->
                if (isConnected) {
                    rootMediaId = serviceConnection.rootMediaId
                    serviceConnection.playbackState.value?.apply {
                        currentPlaybackPosition = position
                    }
                    serviceConnection.subscribe(rootMediaId, subscriptionCallback)
                }
            }
        }
    }

    val isAudioPlaying: Boolean
        get() = playbackState.value?.isPlaying == true
    val currentSongDuration = MusicService.curSongDuration
    var currentAudioProgress = mutableStateOf(0f)


    private val subscriptionCallback =
        object : MediaBrowserCompat.SubscriptionCallback() {

            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
            }
        }

    private val serviceConnection = serviceConnection.also {
        updatePlayback()
    }

    fun saveCurrentPlayingAlbumId(albumId: Int) = viewModelScope.launch {
        interactor.saveLastPlayingAlbum(albumId)
    }

    private fun FetchResult<List<AppAudioTrack>>.toUiState(): UiState<List<AudioTrackUi>> =
        when (this) {
            is FetchResult.Success -> {
                UiState.Success(
                    data?.map { trackDomain ->
                        AudioTrackUi.fromDomain(trackDomain)
                    }
                )
            }
            is FetchResult.Fail -> {
                UiState.Fail(
                    message = errorHandler.getErrorMessage(error)
                )
            }
        }

    fun playPauseToggle() {
        val uiState = uiState.value
        if (uiState !is UiState.Success || uiState.trackList == null) return // todo handle some error message
        serviceConnection.playAudio(uiState.trackList)

        if (currentPlayingAudio.value != null) {
            if (isAudioPlaying) {
                serviceConnection.transportControl.pause()
            } else {
                serviceConnection.transportControl.play()
            }
        } else {
            val trackToPlay = uiState.trackList.first()
            serviceConnection.transportControl.playFromMediaId(
                trackToPlay.uri.toString(),
                null
            )
        }
    }

    fun stopPlayback() {
        serviceConnection.transportControl.stop()
    }

    fun fastForward() {
        serviceConnection.fastForward()
    }

    fun rewind() {
        serviceConnection.rewind()
    }

    fun skipToNext() {
        serviceConnection.skipToNext()
    }

    fun seekTo(value: Float) {
        serviceConnection.transportControl.seekTo(
            (currentSongDuration * value / 100f).toLong()
        )
    }

    private fun updatePlayback() {
        viewModelScope.launch {
            val position = playbackState.value?.currentPosition ?: 0
            if (currentPlaybackPosition != position) {
                currentPlaybackPosition = position
            }
            if (currentSongDuration > 0) {
                currentAudioProgress.value =
                    currentPlaybackPosition.toFloat() / currentSongDuration.toFloat() * 100f
            }
            delay(Consts.PLAYBACK_UPDATE_INTERVAL)
            if (updatePosition) {
                updatePlayback()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        serviceConnection.unsubscribe(
            Consts.MY_MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {}
        )
        updatePosition = false
    }

    fun togglePlayerAnimationState(state: PlayerState) {
//        _playerAnimationState.value = when (state) {
//            PlayerState.STOPPED, PlayerState.STOPPING -> PlayerState.STARTING
//            PlayerState.STARTED, PlayerState.STARTING -> PlayerState.STOPPING
//        }
    }

    fun resumePlayerAnimationStateFrom(oldState: PlayerState) {
        _playerAnimationState.value = when (oldState) {
            PlayerState.STOPPING -> PlayerState.STOPPED
            PlayerState.STARTING -> PlayerState.STARTED
            else -> oldState

        }
    }

    fun composableIsVisible() {
        shouldShowSmoothStartAndStopVynilAnimation.value = true
    }

    fun composableIsInvisible() {
        shouldShowSmoothStartAndStopVynilAnimation.value = false
    }
}