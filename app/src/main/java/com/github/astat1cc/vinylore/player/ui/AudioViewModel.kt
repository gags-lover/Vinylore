package com.github.astat1cc.vinylore.player.ui

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.runtime.mutableStateOf
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
import com.github.astat1cc.vinylore.player.ui.tonearm.TonearmState
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

    private val _playerAnimationState = MutableStateFlow(PlayerState.STOPPED)
    val playerAnimationState = _playerAnimationState.asStateFlow()

    private val _tonearmAnimationState =
        MutableStateFlow<TonearmState>(TonearmState.ON_START_POSITION)
    val tonearmAnimationState: StateFlow<TonearmState> = _tonearmAnimationState.asStateFlow()

    private val _currentPlaybackPosition: MutableStateFlow<Long> =
        MutableStateFlow(0L)
    val currentPlaybackPosition = _currentPlaybackPosition.asStateFlow()

    private val currentPlayingAudio: StateFlow<AudioTrackUi?> =
        serviceConnection.currentPlayingAudio

    private val isConnected: StateFlow<Boolean> =
        serviceConnection.isConnected

    private var updatePosition = true

    private lateinit var rootMediaId: String

    private val playbackState: StateFlow<PlaybackStateCompat?> =
        serviceConnection.playbackState // todo make flow of only needed variables

    private val isPlaying: StateFlow<Boolean> = playbackState.map { state ->
        state?.isPlaying == true
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val shouldShowSmoothStartAndStopVinylAnimation = MutableStateFlow(false)

    val currentSongDuration = MusicService.curSongDuration
    var currentAudioProgress = mutableStateOf(0f)


    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {

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

    init {
        viewModelScope.launch {
            this@AudioViewModel.isPlaying.collect { isPlaying ->
                if (isPlaying) {
                    if (shouldShowSmoothStartAndStopVinylAnimation.value) {
                        tryEmitPlayerState(PlayerState.STARTING)
                    } else {
                        tryEmitPlayerState(PlayerState.STARTED)
                    }
                } else {
                    tryEmitPlayerState(PlayerState.STOPPED)
                }
            }
        }
        viewModelScope.launch {
            isConnected.collect { isConnected ->
                if (isConnected) {
                    rootMediaId = serviceConnection.rootMediaId
                    serviceConnection.playbackState.value?.apply {
                        _currentPlaybackPosition.value = position
                    }
                    serviceConnection.subscribe(rootMediaId, subscriptionCallback)
                }
            }
        }
    }

    fun playPauseToggle() {
        val uiState = uiState.value
        if (uiState !is UiState.Success || uiState.trackList == null) return // todo handle some error message

        if (currentPlayingAudio.value != null) {
            if (isPlaying.value) {
                serviceConnection.slowPause()
                tryEmitPlayerState(PlayerState.STOPPING)
            } else {
                serviceConnection.slowResume()
                tryEmitPlayerState(PlayerState.STARTING)
            }
        } else {
            serviceConnection.setTrackList(uiState.trackList)
            tryEmitPlayerState(PlayerState.STARTING)
            viewModelScope.launch {
                delay(1500L)
                _tonearmAnimationState.value = TonearmState.MOVING_TO_DISC
            }
        }
    }

    private fun tryEmitPlayerState(newState: PlayerState) {
        val oldState = _playerAnimationState.value
        _playerAnimationState.value = when (newState) {
            PlayerState.STARTING -> if (oldState != PlayerState.STARTED) newState else oldState
            PlayerState.STOPPING -> if (oldState != PlayerState.STOPPED) newState else oldState
            else -> newState
        }
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
            if (_currentPlaybackPosition.value != position) {
                _currentPlaybackPosition.value = position
            }
            if (currentSongDuration > 0) {
                currentAudioProgress.value =
                    _currentPlaybackPosition.value.toFloat() / currentSongDuration.toFloat() * 100f
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
            subscriptionCallback
        )
        updatePosition = false
    }

    fun resumePlayerAnimationStateFrom(oldState: PlayerState) {
        tryEmitPlayerState(
            when (oldState) {
                PlayerState.STARTING -> PlayerState.STARTED
                PlayerState.STOPPING -> PlayerState.STOPPED
                else -> return
            }
        )
    }

    fun resumeTonearmAnimationStateFrom(oldState: TonearmState) {
        _tonearmAnimationState.value = when (oldState) {
            TonearmState.MOVING_TO_DISC -> TonearmState.STAYING_ON_DISC
            TonearmState.MOVING_FROM_DISC -> TonearmState.ON_START_POSITION
            TonearmState.MOVING_ON_DISC -> TonearmState.MOVING_FROM_DISC
            else -> oldState
        }
    }

    fun composableIsVisible() {
        shouldShowSmoothStartAndStopVinylAnimation.value = true
    }

    fun composableIsInvisible() {
        shouldShowSmoothStartAndStopVinylAnimation.value = false
    }
}