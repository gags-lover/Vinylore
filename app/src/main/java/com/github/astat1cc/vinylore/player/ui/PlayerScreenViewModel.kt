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
import com.github.astat1cc.vinylore.core.models.domain.FetchResult
import com.github.astat1cc.vinylore.core.models.ui.PlayingAlbumUi
import com.github.astat1cc.vinylore.core.models.ui.AudioTrackUi
import com.github.astat1cc.vinylore.player.domain.MusicPlayerInteractor
import com.github.astat1cc.vinylore.player.ui.models.PlayerScreenUiStateData
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

    val trackIsJustPrepared: StateFlow<Boolean?> = serviceConnection.trackIsJustPrepared

    // should not call prepare if task was removed but service is still working
    // and should if app was just opened and it needs to call first preparing
    private val shouldCallPrepare =
        !taskWasRemoved.value || // without "!" it means task was killed but service wasn't
                playbackState.value?.let { playbackState ->
                    // it means service was killed but task wasn't
                    !playbackState.isPrepared
                } ?: true

    init {
        Log.e("meta","init vm: ${this.hashCode()}, should $shouldCallPrepare")
        launchPlaybackUpdating()
        with(viewModelScope) {
            launch {
                serviceConnection.shouldRefreshMusicService.collect { should ->
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
                uiState.collect { uiState ->
                    if (uiState !is UiState.Success) return@collect

//                    Log.e(
//                        "meta",
//                        "collecting uiState ${uiState.data.album}, taskRemoved ${MusicService.taskWasRemoved.value}, vm: ${this@PlayerScreenViewModel.hashCode()}"
//                    )
                    if (!shouldCallPrepare) return@collect
                    var prepareCalled = false
                    while (!prepareCalled) {
                        if (uiState.data.album != null &&
                            isConnected.value
                        ) {
                            serviceConnection.prepareMedia(uiState.data.album)
                            prepareCalled = true
                        }
                        delay(1000L)
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

    private var tonearmRotationConnectionDelay = 100L // todo delete

    private var usualProgressSyncJob: Job? = null
    private fun startUsualProgressSync() {
        sliderDraggingProgressSyncJob?.cancel()
        usualProgressSyncJob = viewModelScope.launch {
            while (true) {
                if (!trackIsChanging && shouldStartSynchronizeTonearmRotationWithProgress) {
                    _tonearmRotation.value = getRotationFrom(_currentTrackProgress.value)
                }
                delay(100L)
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
        if (uiState.value !is UiState.Success) return // todo handle some error message
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

    private fun FetchResult<AppPlayingAlbum?>.toUiState(): UiState<PlayerScreenUiStateData> =
        when (this) {
            is FetchResult.Success -> {
                val album = if (data == null) null else PlayingAlbumUi.fromDomain(data)
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
        viewModelScope.launch {
            trackIsChanging = true
            serviceConnection.skipToNext()?.join()
            if (_tonearmAnimationState.value != TonearmState.IDLE_POSITION) {
                while (trackIsChanging) {
                    if (_currentTrackProgress.value < 2f) {
                        trackIsChanging = false
                    }
                    delay(100L)
                }
            }
        }
    }

    fun skipToPrevious(currentTrackQueuePosition: Long?) {
        if (currentTrackQueuePosition == null) return // that means uiState is not Success
        viewModelScope.launch {
            trackIsChanging = true
            serviceConnection.skipToPrevious(currentTrackQueuePosition)?.join()
            if (_tonearmAnimationState.value != TonearmState.IDLE_POSITION) {
                while (trackIsChanging) {
                    if (_currentTrackProgress.value < 2f) {
                        trackIsChanging = false
                    }
                    delay(100L)
                }
            }
//        if (currentTrackQueuePosition == null) return // that means uiState is not Success
//        viewModelScope.launch {
//            shouldStartSynchronizeTonearmRotationWithProgress = false
//            _tonearmLifted.value = true
//            _tonearmAnimationState.value = TonearmState.MOVING_FROM_DISC_TO_START_POSITION
//            serviceConnection.skipToPrevious(currentTrackQueuePosition = currentTrackQueuePosition)
//                .join()
//            _tonearmLifted.value = false
//            while (!shouldStartSynchronizeTonearmRotationWithProgress) {
//                if (_currentPlaybackPosition.value < 2000f) {
//                    Log.e(
//                        "tonearm",
//                        "should be less than 2f: " + _currentTrackProgress.value.toString()
//                    )
//                    shouldStartSynchronizeTonearmRotationWithProgress = true
//                    _tonearmAnimationState.value = TonearmState.MOVING_ABOVE_DISC
//                }
//                delay(100L)
//            }
//        }
        }
    }

    private var draggingSliderForTheFirstTime = true
    fun dragSlider(newValue: Float) {
        if (trackIsChanging) return
        if (draggingSliderForTheFirstTime) {
            draggingSliderForTheFirstTime = false
            updatePosition = false
            startSliderDraggingProgressSync()
            _tonearmLifted.value = true
            serviceConnection.muteVolume()
        }
        _currentTrackProgress.value = newValue
    }

    fun sliderDraggingFinished() {
        serviceConnection.transportControl.seekTo(
            (currentSongDuration.value * currentTrackProgress.value / 100f).toLong()
        )
        serviceConnection.unmuteVolume()
        startUsualProgressSync()
        _tonearmLifted.value = false
        updatePosition = true
        draggingSliderForTheFirstTime = true
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
        _albumChoosingCalled.value = true
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