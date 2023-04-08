package com.github.astat1cc.vinylore.player.ui

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.astat1cc.vinylore.ServiceConsts
import com.github.astat1cc.vinylore.core.AppConst
import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.models.domain.AppPlayingAlbum
import com.github.astat1cc.vinylore.core.models.domain.ErrorType
import com.github.astat1cc.vinylore.core.models.domain.FetchResult
import com.github.astat1cc.vinylore.core.models.ui.PlayingAlbumUi
import com.github.astat1cc.vinylore.core.models.ui.AudioTrackUi
import com.github.astat1cc.vinylore.player.domain.MusicPlayerInteractor
import com.github.astat1cc.vinylore.player.ui.service.*
import com.github.astat1cc.vinylore.player.ui.views.tonearm.TonearmState
import com.github.astat1cc.vinylore.player.ui.views.vinyl.VinylDiscState
import com.github.astat1cc.vinylore.core.models.ui.UiState
import kotlinx.coroutines.Job
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

    val uiState: StateFlow<UiState<PlayingAlbumUi>> =
        interactor.getAlbumFlow()
            .map { fetchResult ->
                fetchResult?.toUiState() ?: UiState.Loading()
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Loading())

    private val _shouldNavigateToAlbumChoosing = MutableStateFlow<Boolean>(false)
    val shouldNavigateToAlbumChoosing: StateFlow<Boolean> =
        _shouldNavigateToAlbumChoosing.asStateFlow()

    private val customPlayerState: StateFlow<CustomPlayerState> =
        serviceConnection.customPlayerState

    private val _vinylAnimationState = MutableStateFlow(VinylDiscState.STOPPED)
    val vinylAnimationState = _vinylAnimationState.asStateFlow()

    private val _tonearmAnimationState = MutableStateFlow(TonearmState.IDLE_POSITION)
    val tonearmAnimationState: StateFlow<TonearmState> = _tonearmAnimationState.asStateFlow()

    private val _vinylRotation = MutableStateFlow<Float>(0f)
    val vinylRotation = _vinylRotation.asStateFlow()

    private val _tonearmRotation = MutableStateFlow<Float>(0f)
    val tonearmRotation: StateFlow<Float> = _tonearmRotation.asStateFlow()

    private val _shouldRefreshScreen = MutableStateFlow(false)
    val shouldRefreshScreen: StateFlow<Boolean> = _shouldRefreshScreen.asStateFlow()
//        .map {
//        Log.e("refresh", "$it vm: ${this.hashCode()} sc: ${serviceConnection.hashCode()} ")
//        it
//    }.shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

// added to escape situations when asynchronous animatable changing rotation even after stop
// (situation when navigating to another album)
//    var isRotationChangingAble = true
//    var isRotationChangingAble = false

    private val _currentPlaybackPosition: MutableStateFlow<Long> =
        MutableStateFlow(0L)
    val currentPlaybackPosition = _currentPlaybackPosition.asStateFlow()

//    private val albumChosenRecently = serviceConnection.albumChosenRecently

    val currentPlayingTrack: StateFlow<AudioTrackUi?> = serviceConnection.currentPlayingTrack

    private val isConnected: StateFlow<Boolean> = serviceConnection.isConnected

    private var updatePosition = true
    private var updatePositionLoopRequired = true

    private lateinit var rootMediaId: String

    private val playbackState: StateFlow<PlaybackStateCompat?> =
        serviceConnection.playbackState

    private val isMusicPlaying: StateFlow<Boolean> = serviceConnection.isMusicPlaying

    private val shouldShowSmoothStartAndStopVinylAnimation = MutableStateFlow(false)

    private val currentSongDuration = MusicService.curSongDuration

    private val _currentTrackProgress = MutableStateFlow<Float>(0f)
    val currentTrackProgress: StateFlow<Float> = _currentTrackProgress.asStateFlow()

    val showPlayIconAtPlayPauseToggle = _vinylAnimationState.map { state ->
        state == VinylDiscState.STOPPING || state == VinylDiscState.STOPPED
    }.stateIn(viewModelScope, SharingStarted.Lazily, true)

    val playPauseToggleBlocked =
        _vinylAnimationState.combine(customPlayerState) { animationState, customPlayerState ->
            animationState == VinylDiscState.STOPPING ||
                    animationState == VinylDiscState.STARTING ||
                    customPlayerState == CustomPlayerState.LAUNCHING ||
                    customPlayerState == CustomPlayerState.CHANGING_TRACK
        }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val sliderEnabled = customPlayerState.map { state ->
        !(state == CustomPlayerState.IDLE ||
                state == CustomPlayerState.LAUNCHING ||
                state == CustomPlayerState.CHANGING_TRACK ||
                state == CustomPlayerState.SEEKING_TO_BEGINNING)
    }

    private val taskWasRemoved = MusicService.taskWasRemoved

    private var shouldStartSynchronizeTonearmRotationWithProgress = false
    private var trackIsChanging = false

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

    val showVinyl: StateFlow<Boolean?> = serviceConnection.showVinyl

    // should not call prepare if task was removed but service is still working
    // and should if app was just opened and it needs to call first preparing
    private val shouldCallPrepare =
        !taskWasRemoved.value || // without "!" it means task was killed but service wasn't
                playbackState.value?.let { playbackState ->
                    // it means service was killed but task wasn't
                    !playbackState.isPrepared
                } ?: true

    init {
        launchPlaybackUpdating()
        with(viewModelScope) {
            launch {
                serviceConnection.shouldRefreshMusicService.collect { should ->
                    // Because connection doesn't know about state Fail, and it thinks screen should
                    // be refreshed. Also app can throw CancellationException which should be ignored
                    // here and screen should be refreshed that case.
                    val state = uiState.value
                    if (state is UiState.Fail &&
                        state.message != AppErrorHandler.Impl.CancellationErrorMessage
                    ) return@collect

                    _shouldRefreshScreen.value = should
                }
            }
            launch {
                // delay to give time previous PlayerScreen's viewmodel to call onCleared() and
                // emit IDLE to CustomPlayerState
                delay(INITIAL_DELAY_FOR_PLAY_BUTTON_BLOCKING)
                initialDelayForPlayButtonBlockingPassed = true
            }
            launch {
                customPlayerState.collect { state ->
                    blockVinylRotation = false
                    when (state) {
                        CustomPlayerState.IDLE -> {
                            tryEmitPlayerAnimationState(VinylDiscState.STOPPED)
                            _tonearmAnimationState.value = TonearmState.IDLE_POSITION

                            // because when screen initializing at first state is PLAYING and
                            // rotation is like when it was connected to progress
                            _tonearmRotation.value = 0f

                            shouldStartSynchronizeTonearmRotationWithProgress = false
                            _vinylRotation.value = 0f
                            _tonearmLifted.value = true
                        }
                        CustomPlayerState.LAUNCHING -> {
                            tryEmitPlayerAnimationState(VinylDiscState.STARTING)
                            _tonearmAnimationState.value =
                                TonearmState.MOVING_FROM_IDLE_TO_START_POSITION
                            shouldStartSynchronizeTonearmRotationWithProgress = false
                            _tonearmLifted.value = true
                        }
                        CustomPlayerState.PLAYING -> {
                            tryEmitPlayerAnimationState(VinylDiscState.STARTING)
                            _tonearmAnimationState.value = TonearmState.MOVING_ABOVE_DISC
                            _tonearmLifted.value = false
                            // because in cases when seeking to beginning
//                            delay(1000L)
                            shouldStartSynchronizeTonearmRotationWithProgress = true
                        }
                        CustomPlayerState.PAUSED -> {
                            tryEmitPlayerAnimationState(VinylDiscState.STOPPING)
                            _tonearmAnimationState.value = TonearmState.STAYING_ON_DISC
                            shouldStartSynchronizeTonearmRotationWithProgress = true
//                            _tonearmLifted.value = false
                        }
                        CustomPlayerState.TURNING_OFF -> {
                            tryEmitPlayerAnimationState(VinylDiscState.STOPPING)
                            _tonearmAnimationState.value =
                                TonearmState.MOVING_FROM_DISC_TO_IDLE_POSITION
                            shouldStartSynchronizeTonearmRotationWithProgress = false
                            _tonearmLifted.value = true
                        }
                        CustomPlayerState.CHANGING_TRACK -> {
                            tryEmitPlayerAnimationState(VinylDiscState.STOPPED)
                            if (_tonearmAnimationState.value != TonearmState.IDLE_POSITION) {
                                _tonearmAnimationState.value =
                                    TonearmState.MOVING_FROM_DISC_TO_START_POSITION
                            }
                            shouldStartSynchronizeTonearmRotationWithProgress = false
                            _tonearmLifted.value = true

                            // this is to make new vinyl rotation equal 0
                            delay(AppConst.VINYL_VISIBILITY_SHRINK_OUT_DURATION.toLong())
                            blockVinylRotation = true
                            _vinylRotation.value = 0f
                        }
                        CustomPlayerState.SEEKING_TO_BEGINNING -> {
                            if (_tonearmAnimationState.value != TonearmState.IDLE_POSITION) {
                                _tonearmAnimationState.value =
                                    TonearmState.MOVING_FROM_DISC_TO_START_POSITION
                            }
                            shouldStartSynchronizeTonearmRotationWithProgress = false
                            _tonearmLifted.value = true
                        }
                        CustomPlayerState.STARTING_AFTER_CHANGING -> {
                            blockVinylRotation = false
                            tryEmitPlayerAnimationState(VinylDiscState.STARTING)
//                            shouldStartSynchronizeTonearmRotationWithProgress = false
                            _tonearmLifted.value = false
                        }
                    }
                }
            }
            launch {
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
            launch {
                // calls prepare every time album changed in uiState
                uiState.collect uiState@{ uiState ->
                    if (uiState !is UiState.Success || !shouldCallPrepare
                    ) return@uiState

                    prepareMedia(album = uiState.data)
                }
            }
            launch {
                currentPlayingTrack.collect { track ->
                    track?.let {
                        serviceConnection.trackChanged()
                    }
                }
            }
            launch {
                if (shouldCallPrepare) interactor.initializeAlbum()
                if (taskWasRemoved.value) MusicService.taskRestored()
                startUsualProgressSync()
            }
        }
    }

    private var preparingJob: Job? = null
    private fun prepareMedia(album: PlayingAlbumUi) {
        preparingJob?.cancel()
        preparingJob = viewModelScope.launch {
            while (true) {
                if (isConnected.value) {
                    serviceConnection.prepareMedia(album)
                    return@launch
                }
                delay(300L)
            }
        }
    }

    private var tonearmRotationConnectionDelay = 100L // todo delete

    private var usualProgressSyncJob: Job? = null
    private fun startUsualProgressSync() {
        sliderDraggingProgressSyncJob?.cancel()
        usualProgressSyncJob = viewModelScope.launch {
            while (true) {
                if (!trackIsChanging &&
                    shouldStartSynchronizeTonearmRotationWithProgress &&
                    !sliderIsDraggingNow
                ) {
                    _tonearmRotation.value = getRotationFrom(_currentTrackProgress.value)
                }

                // not 1 sec because it also affects cases when state is PAUSED and  user just
                // clicks to seek to needed position
                delay(200L)
            }
        }
    }

    private var sliderDraggingProgressSyncJob: Job? = null
    private fun startSliderDraggingProgressSync() {
        usualProgressSyncJob?.cancel()
        sliderDraggingProgressSyncJob = viewModelScope.launch {
            while (true) {
                if (shouldStartSynchronizeTonearmRotationWithProgress) {
                    _tonearmRotation.value = getRotationFrom(_currentTrackProgress.value)
                }
                delay(16L)
            }
        }
    }

    private fun getRotationFrom(percentageProgress: Float) =
// divided by 5.26 to make 100% progress equal to 19 points of rotation, so
        // start rotation amount + this value would give end rotation amount
        AppConst.VINYL_TRACK_START_TONEARM_ROTATION + percentageProgress / 5.26f

    fun playPauseToggle() {
        if (!initialDelayForPlayButtonBlockingPassed) return
        if (uiState.value !is UiState.Success || currentPlayingTrack.value == null) return
        if (vinylAnimationState.value == VinylDiscState.STARTING ||
            vinylAnimationState.value == VinylDiscState.STOPPING
        ) return

        if (customPlayerState.value == CustomPlayerState.IDLE) {
//            isRotationChangingAble = true
            serviceConnection.launchPlaying()
        } else {
            if (isMusicPlaying.value) {
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

    private fun tryEmitPlayerAnimationState(newState: VinylDiscState) {
        val oldState = _vinylAnimationState.value
        _vinylAnimationState.value = when (newState) {
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

    private fun FetchResult<AppPlayingAlbum>.toUiState(): UiState<PlayingAlbumUi> =
        when (this) {
            is FetchResult.Success -> {
                val album = PlayingAlbumUi.fromDomain(data)
                UiState.Success(
                    album
//                    PlayerScreenUiStateData(
//                        album = album,
//                        discChosen = discChosen
//                    )
                )
            }
            is FetchResult.Fail -> {
//                _albumIsNotChosen.value = true
                stopPlayback()
                serviceConnection.handleFailAlbum()
                if (error is ErrorType.NoAlbumSelected) _shouldNavigateToAlbumChoosing.value = true
                UiState.Fail(
                    message = errorHandler.getErrorMessage(error)
                )
            }
        }

    private fun stopPlayback() {
        serviceConnection.stop()
    }

    fun fastForward() {
        serviceConnection.fastForward()
    }

    fun rewind() {
        serviceConnection.rewind()
    }

    fun skipToNext() {
        if (uiState.value !is UiState.Success) return
        viewModelScope.launch {
            trackIsChanging = true
            serviceConnection.skipToNext()?.join()
            if (_tonearmAnimationState.value != TonearmState.IDLE_POSITION) {
                while (trackIsChanging) {
                    Log.e("drag", "${_currentTrackProgress.value}")
                    if (_currentTrackProgress.value < 2f) {
                        Log.e("drag", "emitting track is changing false")
                        trackIsChanging = false
                        return@launch
                    }
                    delay(100L)
                }
            }
            trackIsChanging = false
        }
    }

    fun skipToPrevious(currentTrackQueuePosition: Long?) {
        if (uiState.value !is UiState.Success) return
        if (currentTrackQueuePosition == null) return // that means uiState is not Success
        viewModelScope.launch {
            trackIsChanging = true
            serviceConnection.skipToPrevious(currentTrackQueuePosition)?.join()
            if (_tonearmAnimationState.value != TonearmState.IDLE_POSITION) {
                while (trackIsChanging) {
                    if (_currentTrackProgress.value < 2f) {
                        trackIsChanging = false
                        return@launch
                    }
                    delay(100L)
                }
            }
            trackIsChanging = false
        }
    }

    private var sliderIsDraggingNow = false
    fun dragSlider(newValue: Float) {
        if (trackIsChanging) return
        if (!sliderIsDraggingNow) { // that means dragSlider was called for the first time
            sliderIsDraggingNow = true
            updatePosition = false
            startSliderDraggingProgressSync()
            _tonearmLifted.value = true
            serviceConnection.muteVolume()
        }
        _currentTrackProgress.value = newValue
    }

    fun sliderDraggingFinished() {
        viewModelScope.launch {
            val positionToSeek =
                (currentSongDuration.value * currentTrackProgress.value / 100f).toLong()
            serviceConnection.transportControl.seekTo(positionToSeek)
            serviceConnection.unmuteVolume()
            _tonearmLifted.value = false
            updatePosition = true
            sliderIsDraggingNow = false

            // to escape cases when slider dragging is finished but then it for 1 second comes back
            // to prev position because progress update synchronized.
            delay(1000L)
            startUsualProgressSync()
        }
    }

    private fun launchPlaybackUpdating() {
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
                delay(ServiceConsts.PLAYBACK_UPDATE_INTERVAL)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        serviceConnection.unsubscribe(
            ServiceConsts.MY_MEDIA_ROOT_ID,
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
            TonearmState.MOVING_FROM_IDLE_TO_START_POSITION -> TonearmState.MOVING_ABOVE_DISC
            TonearmState.MOVING_FROM_DISC_TO_IDLE_POSITION -> TonearmState.IDLE_POSITION
            TonearmState.MOVING_ABOVE_DISC -> TonearmState.MOVING_FROM_DISC_TO_IDLE_POSITION
            else -> oldState
        }
    }

    fun shouldShowSmoothStartAndStopVinylAnimation() {
        shouldShowSmoothStartAndStopVinylAnimation.value = true
    }

    fun shouldNotShowSmoothStartAndStopVinylAnimation() {
        shouldShowSmoothStartAndStopVinylAnimation.value = false
    }

    private var blockVinylRotation = false
    fun changeDiscRotationFromAnimation(newRotation: Float) {
        if (blockVinylRotation) return
        _vinylRotation.value = newRotation
    }

    fun albumChoosingCalled() {
        _shouldNavigateToAlbumChoosing.value = false
//        interactor.clearFlow()
    }

    fun changeTonearmRotationFromAnimation(newRotation: Float) {
        _tonearmRotation.value = newRotation
    }

    fun vinylAppearanceAnimationShown() {
        serviceConnection.diskIsShown()
    }

    fun refreshUsed() {
        _shouldRefreshScreen.value = false
    }
}