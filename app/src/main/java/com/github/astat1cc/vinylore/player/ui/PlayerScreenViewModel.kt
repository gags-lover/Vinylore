package com.github.astat1cc.vinylore.player.ui

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.astat1cc.vinylore.Consts
import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.models.domain.AppPlayingAlbum
import com.github.astat1cc.vinylore.core.models.domain.FetchResult
import com.github.astat1cc.vinylore.core.models.ui.PlayingAlbumUi
import com.github.astat1cc.vinylore.core.models.ui.AudioTrackUi
import com.github.astat1cc.vinylore.player.domain.MusicPlayerInteractor
import com.github.astat1cc.vinylore.player.ui.models.PlayerScreenUiStateData
import com.github.astat1cc.vinylore.player.ui.service.*
import com.github.astat1cc.vinylore.player.ui.views.tonearm.TonearmState
import com.github.astat1cc.vinylore.player.ui.views.vinyl.VinylDiscState
import com.github.astat1cc.vinylore.core.models.ui.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val INITIAL_DELAY_FOR_PLAY_BUTTON_BLOCKING = 1000L

// todo when song is changed delay time of progress connecting is still the same as prev one
// todo when changing album player is not idle

class PlayerScreenViewModel(
    private val interactor: MusicPlayerInteractor,
    private val errorHandler: AppErrorHandler,
    private val serviceConnection: MediaPlayerServiceConnection
) : ViewModel() {

    val uiState: StateFlow<UiState<PlayerScreenUiStateData>> =
        interactor.getAlbumFlow()
            .map { fetchResult ->
                fetchResult?.toUiState() ?: UiState.Loading()
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Loading())

    private val _albumChoosingCalled = MutableStateFlow<Boolean>(false)
    val albumChoosingCalled: StateFlow<Boolean> = _albumChoosingCalled.asStateFlow()

    private val customPlayerState: StateFlow<CustomPlayerState> =
        serviceConnection.customPlayerState

    private val _playerAnimationState = MutableStateFlow(VinylDiscState.STOPPED)
    val playerAnimationState = _playerAnimationState.asStateFlow()

    private val _tonearmAnimationState = MutableStateFlow(TonearmState.ON_START_POSITION)
    val tonearmAnimationState: StateFlow<TonearmState> = _tonearmAnimationState.asStateFlow()

    private val _discRotation = MutableStateFlow<Float>(0f)
    val discRotation = _discRotation.asStateFlow()

    private val _tonearmRotation = MutableStateFlow<Float>(0f)
    val tonearmRotation: StateFlow<Float> = _tonearmRotation.asStateFlow()

    private val _shouldRefreshMusicService = MutableStateFlow(false)
    val shouldRefreshMusicService: StateFlow<Boolean> = _shouldRefreshMusicService.asStateFlow()

// added to escape situations when asynchronous animatable changing rotation even after stop
// (situation when navigating to another album)
//    var isRotationChangingAble = true
//    var isRotationChangingAble = false

    private val _currentPlaybackPosition: MutableStateFlow<Long> =
        MutableStateFlow(0L)
    val currentPlaybackPosition = _currentPlaybackPosition.asStateFlow()

//    private val albumChosenRecently = serviceConnection.albumChosenRecently

    val currentPlayingTrackName: StateFlow<AudioTrackUi?> =
        serviceConnection.currentPlayingTrack.map { track ->
            // this checking helps skip moments when user have chosen new album, it navigates to
            // PlayerScreen, but there's previous playing track emitted, and album cover on the
            // vinyl and track name change in user's eys, which is not appropriate.
            if (uiState.value is UiState.Loading) {
                null
            } else {
                track
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val isConnected: StateFlow<Boolean> =
        serviceConnection.isConnected

    private var updatePosition = true
    private var updatePositionLoopRequired = true

    private lateinit var rootMediaId: String

    private val playbackState: StateFlow<PlaybackStateCompat?> =
        serviceConnection.playbackState

    private val isMusicPlaying: StateFlow<Boolean> = serviceConnection.isMusicPlaying

    private val shouldShowSmoothStartAndStopVinylAnimation = MutableStateFlow(false)

    private val currentSongDuration = MusicService.curSongDuration
//        .map { duration ->
//        Log.e("dur", duration.toString())
//        if (duration < 120000) { // if less than 2 minutes
//            120000
//        } else {
//            duration
//        }
//    }.stateIn(viewModelScope, SharingStarted.Lazily, 0L)

    private val _currentTrackProgress = MutableStateFlow<Float>(0f)
    val currentTrackProgress: StateFlow<Float> = _currentTrackProgress.asStateFlow()

    private var shouldStartConnectionTonearmRotationWithProgress = false

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            super.onChildrenLoaded(parentId, children)
        }
    }

    private var initialDelayForPlayButtonBlockingPassed = false

    private var _tonearmLifted = MutableStateFlow(false)
    val tonearmLifted: StateFlow<Boolean> = _tonearmLifted.asStateFlow()

    val albumPreparedRecently: StateFlow<Boolean?> = serviceConnection.albumPreparedRecently

    init {
        updatePlayback()
        with(viewModelScope) {
            launch {
                // delay to give time previous PlayerScreen's viewmodel to call onCleared() and
                // emit IDLE to CustomPlayerState
                delay(INITIAL_DELAY_FOR_PLAY_BUTTON_BLOCKING)
                initialDelayForPlayButtonBlockingPassed = true
            }
            launch {
                interactor.initializeAlbum()
                startProgressConnection()
            }
            launch {
                customPlayerState.collect { state ->
                    when (state) {
                        CustomPlayerState.IDLE -> {
                            tryEmitPlayerAnimationState(VinylDiscState.STOPPED)
                            _tonearmAnimationState.value = TonearmState.ON_START_POSITION

                            // because when screen initializing at first state is PLAYING and
                            // rotation is like when it was connected to progress
                            _tonearmRotation.value = 0f

                            shouldStartConnectionTonearmRotationWithProgress = false
                            _discRotation.value = 0f
                            _tonearmLifted.value = true
                        }
                        CustomPlayerState.LAUNCHING -> {
                            tryEmitPlayerAnimationState(VinylDiscState.STARTING)
                            _tonearmAnimationState.value = TonearmState.MOVING_TO_START_POSITION
                            shouldStartConnectionTonearmRotationWithProgress = false
                            _tonearmLifted.value = true
                        }
                        CustomPlayerState.PLAYING -> {
                            tryEmitPlayerAnimationState(VinylDiscState.STARTING)
                            _tonearmAnimationState.value = TonearmState.MOVING_ABOVE_DISC
                            shouldStartConnectionTonearmRotationWithProgress = true
                            _tonearmLifted.value = false
                        }
                        CustomPlayerState.PAUSED -> {
                            tryEmitPlayerAnimationState(VinylDiscState.STOPPING)
                            _tonearmAnimationState.value = TonearmState.STAYING_ON_DISC
                            shouldStartConnectionTonearmRotationWithProgress = true
                            _tonearmLifted.value = false
                        }
                        CustomPlayerState.TURNING_OFF -> {
                            tryEmitPlayerAnimationState(VinylDiscState.STOPPING)
                            _tonearmAnimationState.value = TonearmState.MOVING_TO_IDLE_POSITION
                            shouldStartConnectionTonearmRotationWithProgress = false
                            _tonearmLifted.value = true
                        }
                    }
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
                    Log.e("connection", "collected from isConnected")
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
                        Log.e("connection", "preparation called from collecting uiState")
                        serviceConnection.prepareMedia(state.data.album)
                        preparationCalled = true
                    }
                    delay(1000L)
                }
            }
        }
    }

    private var tonearmRotationConnectionDelay = 100L

    private suspend fun startProgressConnection() {
        // to make 100 step road from start to end position of tonearm
//                    var delayTime = currentSongDuration.value / 100
        // if delayTime is < 100, then song duration < 10 sec, so to escape too
        // often refreshing and too fast moving of tonearm, better
//                    if (delayTime < 100) delayTime = 100
        while (true) {
            if (shouldStartConnectionTonearmRotationWithProgress) {
                _tonearmRotation.value = getRotationFrom(_currentTrackProgress.value)
//                Log.e("rotation", "${_tonearmRotation.value} ${this@PlayerScreenViewModel.hashCode()}")
            }
            delay(tonearmRotationConnectionDelay)
        }
    }

    private fun getRotationFrom(percentageProgress: Float) =
// divided by 5.26 to make 100% progress equal to 19 points of rotation, so
        // start rotation amount + this would give end rotation amount
        VINYL_TRACK_START_TONEARM_ROTATION + percentageProgress / 5.26f

//    /**
//     * Function to escape cases when short duration song makes tonearm move too fast, so
//     * the function returns such rotation that end of the song would be while tonearm didn't
//     * get to the end of vinyl disc, but speed of moving tonearm would seem normal
//     */
//    private fun getRotationProgressForMinimalTrackDuration(percentageProgress: Float) =
//        VINYL_TRACK_START_TONEARM_ROTATION + percentageProgress / 5.26f

    fun playPauseToggle() {
        if (!initialDelayForPlayButtonBlockingPassed) return
        if (uiState.value !is UiState.Success) return // todo handle some error message

        if (customPlayerState.value == CustomPlayerState.IDLE) {
//            isRotationChangingAble = true
            serviceConnection.launchPlaying()
        } else {
            if (playerAnimationState.value == VinylDiscState.STARTED) {
                serviceConnection.slowPause()
            } else if (playerAnimationState.value == VinylDiscState.STOPPED) {
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

    private fun tryEmitPlayerAnimationState(newState: VinylDiscState) {
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

    private fun FetchResult<AppPlayingAlbum?>.toUiState(): UiState<PlayerScreenUiStateData> =
        when (this) {
            is FetchResult.Success -> {
                val album = if (data == null) null else PlayingAlbumUi.fromDomain(data)
                val discChosen = album != null
                Log.e("album", album?.trackList?.first().toString())
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

    fun skipToPrevious() {
        serviceConnection.skipToPrevious()
    }

    fun sliderDragging(newValue: Float) {
        updatePosition = false
        if (!tonearmLifted.value) {
            tonearmRotationConnectionDelay = 15L
            _tonearmLifted.value = true
        }
        serviceConnection.muteVolume()
        _currentTrackProgress.value = newValue
    }

    fun sliderDraggingFinished() {
        with(serviceConnection.transportControl) {
            seekTo(
                (currentSongDuration.value * currentTrackProgress.value / 100f).toLong()
            )
            serviceConnection.unmuteVolume()
            tonearmRotationConnectionDelay = 100L
        }
        _tonearmLifted.value = false
        updatePosition = true
    }

    private fun updatePlayback() {
        viewModelScope.launch {
            while (updatePositionLoopRequired) {
                if (updatePosition) {
                    val position = playbackState.value?.currentPosition ?: 0
                    if (_currentPlaybackPosition.value != position) {
                        _currentPlaybackPosition.value = position
                    }
                    if (currentSongDuration.value > 0) {
                        _currentTrackProgress.value =
                            _currentPlaybackPosition.value.toFloat() / currentSongDuration.value.toFloat() * 100f
                    }
                }
                delay(Consts.PLAYBACK_UPDATE_INTERVAL)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        serviceConnection.unsubscribe(
            Consts.MY_MEDIA_ROOT_ID,
            subscriptionCallback
        )
        updatePositionLoopRequired = false
    }

    fun resumePlayerAnimationStateFrom(oldState: VinylDiscState) {
        tryEmitPlayerAnimationState(
            if (oldState == VinylDiscState.STARTING) {
                VinylDiscState.STARTED
            } else {
                VinylDiscState.STOPPED
            }
            // in case if fun is called from other states, but i guess it doesn`t happen
//            when (oldState) {
//                VinylDiscState.STARTING -> VinylDiscState.STARTED
//                VinylDiscState.STOPPING -> VinylDiscState.STOPPED
//                else -> return
//            }
        )
    }

    fun resumeTonearmAnimationStateFrom(oldState: TonearmState) {
        _tonearmAnimationState.value = when (oldState) {
            TonearmState.MOVING_TO_START_POSITION -> TonearmState.MOVING_ABOVE_DISC
            TonearmState.MOVING_TO_IDLE_POSITION -> TonearmState.ON_START_POSITION
            TonearmState.MOVING_ABOVE_DISC -> TonearmState.MOVING_TO_IDLE_POSITION
            else -> oldState
        }
    }

    private var refreshCalled = false
    fun composableIsVisible() {
        shouldShowSmoothStartAndStopVinylAnimation.value = true
        if (uiState.value !is UiState.Success && !refreshCalled && appWasHidden) {
            refreshCalled = true
            _shouldRefreshMusicService.value = true
            Log.e("refresh", "Refresh called")
        }
        appWasHidden = false
    }

    private var appWasHidden = false
    fun composableIsInvisible() {
        shouldShowSmoothStartAndStopVinylAnimation.value = false
        appWasHidden = true
    }

    fun changeDiscRotationFromAnimation(newRotation: Float) {
//        if (!isRotationChangingAble) return
//        Log.e("rotation", "$newRotation")
        _discRotation.value = newRotation
    }

    fun albumChoosingCalled() {
        _albumChoosingCalled.value = true
//        interactor.clearFlow()
    }

    fun changeTonearmRotationFromAnimation(newRotation: Float) {
        _tonearmRotation.value = newRotation
    }

    fun vinylAppearanceAnimationShown() {
        serviceConnection.albumPreparedLongAgo()
    }

    companion object {
        const val VINYL_TRACK_START_TONEARM_ROTATION = 19f
    }
}