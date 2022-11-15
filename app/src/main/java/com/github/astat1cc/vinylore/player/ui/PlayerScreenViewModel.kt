package com.github.astat1cc.vinylore.player.ui

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.astat1cc.vinylore.Consts
import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.models.domain.AppAlbum
import com.github.astat1cc.vinylore.core.models.domain.AppAudioTrack
import com.github.astat1cc.vinylore.core.models.domain.FetchResult
import com.github.astat1cc.vinylore.core.models.ui.AlbumUi
import com.github.astat1cc.vinylore.core.models.ui.AudioTrackUi
import com.github.astat1cc.vinylore.player.domain.MusicPlayerInteractor
import com.github.astat1cc.vinylore.player.ui.models.PlayerScreenUiStateData
import com.github.astat1cc.vinylore.player.ui.service.*
import com.github.astat1cc.vinylore.player.ui.tonearm.TonearmState
import com.github.astat1cc.vinylore.player.ui.vinyl.VinylDiscState
import com.github.astat1cc.vinylore.core.models.ui.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlayerScreenViewModel(
    private val interactor: MusicPlayerInteractor,
    private val errorHandler: AppErrorHandler,
    private val serviceConnection: MediaPlayerServiceConnection
) : ViewModel() {

    val uiState: StateFlow<UiState<PlayerScreenUiStateData>> =
        // todo it's collecting the same value every time, fix this
        interactor.fetchTrackList()
            .map { fetchResult -> fetchResult.toUiState() }
            .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Loading())

    private val playerState: StateFlow<PlayerState> = serviceConnection.playerState

    private val _playerAnimationState = MutableStateFlow(VinylDiscState.STOPPED)
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
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

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

    init {
        updatePlayback()
        viewModelScope.launch {
            playerState.collect { state ->
                when (state) {
                    PlayerState.IDLE -> {
                        tryEmitPlayerState(VinylDiscState.STOPPED)
                        _tonearmAnimationState.value = TonearmState.ON_START_POSITION
                    }
                    PlayerState.LAUNCHING -> {
                        tryEmitPlayerState(VinylDiscState.STARTING)
                        _tonearmAnimationState.value = TonearmState.MOVING_TO_DISC
                    }
                    PlayerState.PLAYING -> {
                        tryEmitPlayerState(VinylDiscState.STARTING)
                        _tonearmAnimationState.value = TonearmState.STAYING_ON_DISC
                    }
                    PlayerState.PAUSED -> {
                        tryEmitPlayerState(VinylDiscState.STOPPING)
                        _tonearmAnimationState.value = TonearmState.STAYING_ON_DISC
                    }
                    PlayerState.TURNING_OFF -> {
                        tryEmitPlayerState(VinylDiscState.STOPPING)
                        _tonearmAnimationState.value = TonearmState.MOVING_FROM_DISC
                    }
                }
            }
        }
//        viewModelScope.launch {
//            this@AudioViewModel.isPlaying.collect { isPlaying ->
//                if (isPlaying) {
//                    if (shouldShowSmoothStartAndStopVinylAnimation.value) {
//                        tryEmitPlayerState(VinylDiscState.STARTING)
//                    } else {
//                        tryEmitPlayerState(VinylDiscState.STARTED)
//                    }
//                } else {
//                    tryEmitPlayerState(VinylDiscState.STOPPED)
//                }
//            }
//        }
        viewModelScope.launch {
            isConnected.collect { isConnected ->
                if (isConnected) {
                    rootMediaId = serviceConnection.rootMediaId
                    serviceConnection.playbackState.value?.apply {
                        _currentPlaybackPosition.value = position
                    }
                    serviceConnection.subscribe(rootMediaId, subscriptionCallback)
                    Log.e("tag", "connected")
                }
            }
        }
        viewModelScope.launch {
            uiState.collect { state ->
                if (state !is UiState.Success) return@collect
                var preparationCalled = false
                while (!preparationCalled) {
                    if (state.data.album != null &&
                        isConnected.value
                    ) {
                        serviceConnection.prepareMedia(state.data.album)
                        preparationCalled = true
                    }
                    delay(1000L)
                }
            }
        }
    }

    fun playPauseToggle() {
        val uiState = uiState.value
        if (uiState !is UiState.Success) return // todo handle some error message

        if (playerState.value == PlayerState.IDLE) {
            serviceConnection.launchPlayer()
        } else {
            if (isPlaying.value) {
                serviceConnection.slowPause()
            } else {
                serviceConnection.slowResume()
            }
        }
//        if (currentPlayingAudio.value != null) {
//            if (isPlaying.value) {
//                serviceConnection.slowPause()
////                tryEmitPlayerState(VinylDiscState.STOPPING)
//            } else {
//                serviceConnection.slowResume()
////                tryEmitPlayerState(VinylDiscState.STARTING)
//            }
//        } else {
//            serviceConnection.launchPlayer()
////            tryEmitPlayerState(VinylDiscState.STARTING)
////            _tonearmAnimationState.value = TonearmState.MOVING_TO_DISC
//        }
    }

    private fun tryEmitPlayerState(newState: VinylDiscState) {
        val oldState = _playerAnimationState.value
        _playerAnimationState.value = when (newState) {
            VinylDiscState.STARTING ->
                if (shouldShowSmoothStartAndStopVinylAnimation.value) {
                    if (oldState != VinylDiscState.STARTED) newState else oldState
                } else {
                    VinylDiscState.STARTED
                }

            VinylDiscState.STOPPING ->
                if (shouldShowSmoothStartAndStopVinylAnimation.value) {
                    if (oldState != VinylDiscState.STOPPED) newState else oldState
                } else {
                    VinylDiscState.STOPPED
                }
            else -> newState
        }
    }

    private fun FetchResult<AppAlbum?>.toUiState(): UiState<PlayerScreenUiStateData> =
        when (this) {
            is FetchResult.Success -> {
                val album = if (data == null) data else AlbumUi.fromDomain(data)
                val discChosen = album != null
                UiState.Success(
                    PlayerScreenUiStateData(
                        album = album,
                        discChosen = discChosen
                    )
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

    fun resumePlayerAnimationStateFrom(oldState: VinylDiscState) {
        tryEmitPlayerState(
            when (oldState) {
                VinylDiscState.STARTING -> VinylDiscState.STARTED
                VinylDiscState.STOPPING -> VinylDiscState.STOPPED
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