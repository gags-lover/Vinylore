package com.github.astat1cc.vinylore.player.ui.service

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.github.astat1cc.vinylore.ServiceConsts
import com.github.astat1cc.vinylore.core.AppConst
import com.github.astat1cc.vinylore.core.models.ui.PlayingAlbumUi
import com.github.astat1cc.vinylore.core.models.ui.AudioTrackUi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

// todo handle queue ending

class MediaPlayerServiceConnection(
    context: Context
) {

    private val _customPlayerState = MutableStateFlow<CustomPlayerState>(CustomPlayerState.IDLE)
    val customPlayerState = _customPlayerState.asStateFlow()

    private val _playbackState = MutableStateFlow<PlaybackStateCompat?>(null)
    val playbackState: StateFlow<PlaybackStateCompat?> = _playbackState.asStateFlow()

    fun handleFailAlbum() {
        _playingAlbum.value = null
    }

    private val _isConnected = MutableStateFlow<Boolean>(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _currentPlayingTrack = MutableStateFlow<AudioTrackUi?>(null)
    val currentPlayingTrack = _currentPlayingTrack.asStateFlow()

    private val _playingAlbum = MutableStateFlow<PlayingAlbumUi?>(null)
    val playingAlbum: StateFlow<PlayingAlbumUi?> = _playingAlbum.asStateFlow()

    private val _showVinyl = MutableStateFlow<Boolean?>(null)
    val showVinyl: StateFlow<Boolean?> = _showVinyl.asStateFlow()

    private val _shouldRefreshMusicService = MutableSharedFlow<Boolean>(replay = 0)
    val shouldRefreshMusicService: SharedFlow<Boolean> = _shouldRefreshMusicService.asSharedFlow()

    private val connectionScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var mediaController: MediaControllerCompat

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)
    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(context, MusicService::class.java),
        mediaBrowserConnectionCallback,
        null
    ).apply {
        connect()
    }

    val rootMediaId: String
        get() = mediaBrowser.root

    val transportControl: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    val isMusicPlaying = playbackState.map { state ->
        state?.isPlaying == true
    }.stateIn(connectionScope, SharingStarted.Eagerly, false)

    init {
        connectionScope.launch {
            // this is to connect service playback state and player state animation when user paused
            // or resumed music from the notification
            isMusicPlaying.collect { isPlaying ->
                if (isPlaying) {
                    emitPlayerState(CustomPlayerState.PLAYING)
                } else if (!isPlaying) {
                    emitPlayerState(CustomPlayerState.PAUSED)
                }
            }
        }
    }

    fun slowPause() {
        if (_customPlayerState.value != CustomPlayerState.PLAYING) return
        mediaBrowser.sendCustomAction(ServiceConsts.SLOWLY_PAUSE_MEDIA_PLAY_ACTION, null, null)
        emitPlayerState(CustomPlayerState.PAUSED)
    }

    fun slowResume() {
        if (_customPlayerState.value != CustomPlayerState.PAUSED) return
        mediaBrowser.sendCustomAction(ServiceConsts.SLOWLY_RESUME_MEDIA_PLAY_ACTION, null, null)
//        emitPlayerState(CustomPlayerState.PLAYING)
    }

    private var muteCalled = false
    fun muteVolume() {
        if (muteCalled) return
        mediaBrowser.sendCustomAction(ServiceConsts.MUTE, null, null)
        muteCalled = true
    }

    fun unmuteVolume() {
        mediaBrowser.sendCustomAction(ServiceConsts.UNMUTE, null, null)
        muteCalled = false
    }

    private var albumChanged = false
    fun prepareMedia(album: PlayingAlbumUi) {
        playerLaunchingJob?.cancel()
        emitPlayerState(CustomPlayerState.IDLE)
        _showVinyl.value = false
        _playingAlbum.value = album
        mediaBrowser.sendCustomAction(
            ServiceConsts.PREPARE_MEDIA_ACTION,
            null,
            null
        ) // todo if i need this?
        val trackToPlay = playingAlbum.value!!.trackList[0]
        transportControl.prepareFromMediaId(
            trackToPlay.uri.toString(),
            null
        )
        albumChanged = true
    }

    /**
     * Function called by viewmodel when it collects new non null currentPlayingTrack to add delay
     * before vinyl's slideIn.
     */
    suspend fun trackChanged() {
        if (albumChanged) {
            delay(400L)
            _showVinyl.value = true
            albumChanged = false
        }
    }

    private fun emitPlayerState(newState: CustomPlayerState) {
        val oldState = _customPlayerState.value
        if (blockChangingPlayerState) return
        if (oldState == newState ||
            (oldState == CustomPlayerState.IDLE && newState == CustomPlayerState.PAUSED)
        ) return
        _customPlayerState.value = newState
    }

    private var playerLaunchingJob: Job? = null
    fun launchPlaying() {
        emitPlayerState(CustomPlayerState.LAUNCHING)
        playerLaunchingJob?.cancel()
        playerLaunchingJob = connectionScope.launch {
            delay(3200L)
            mediaBrowser.sendCustomAction(ServiceConsts.START_CRACKLE_ACTION, null, null)
            emitPlayerState(CustomPlayerState.PLAYING)
            delay(2500L)
            mediaBrowser.sendCustomAction(ServiceConsts.START_TRACK_PLAYING_ACTION, null, null)
        }
    }

    fun fastForward(seconds: Int = 10) {
        playbackState.value?.currentPosition?.let {
            transportControl.seekTo(it + seconds * 1000)
        }
    }

    fun rewind(seconds: Int = 10) {
        playbackState.value?.currentPosition?.let {
            transportControl.seekTo(it - seconds * 1000)
        }
    }

    //    private var trackWasChanged = false
    private var blockChangingPlayerState = false
    private var blockSkipping = false
    private var skippingJob: Job? = null
    fun skipToNext(): Job? {
        if (blockSkipping) return null // stopping multiple clicks
        blockSkipping = true
        skippingJob?.cancel()
//        skippingJob?.isCancelled
        skippingJob = connectionScope.launch {
            val stateBeforeChanging = _customPlayerState.value
            emitPlayerState(CustomPlayerState.CHANGING_TRACK)
            blockChangingPlayerState = true
            mediaBrowser.sendCustomAction(ServiceConsts.PAUSE_MEDIA_PLAY_ACTION, null, null)
            _showVinyl.value = false // vinyl shrinks out
            delay(AppConst.TONEARM_MOVING_FROM_DISC_TO_START_POSITION_DURATION + 50L)
            transportControl.skipToNext()
            delay(AppConst.DELAY_AFTER_SKIPPING_BEFORE_SHOWING_VINYL) // to give more time to call all the mediaChanged calls and improve performance
            _showVinyl.value = true // vinyl slides in
            blockChangingPlayerState = false
            if (stateBeforeChanging == CustomPlayerState.IDLE) {
                emitPlayerState(stateBeforeChanging)
                blockSkipping = false
                return@launch
            }
            blockSkipping = false
            delay(AppConst.SLIDE_IN_DURATION + 150L) // vinyl slides in + some additional delay
            emitPlayerState(CustomPlayerState.STARTING_AFTER_CHANGING)
            delay(100L)
            mediaBrowser.sendCustomAction(ServiceConsts.START_CRACKLE_ACTION, null, null)
            delay(AppConst.VINYL_STARTING_ANIMATION_DURATION + 500L) // vinyl starting and spins for 0.5 sec
//        transportControl.play()
            mediaBrowser.sendCustomAction(ServiceConsts.START_TRACK_PLAYING_ACTION, null, null)

            emitPlayerState(CustomPlayerState.PLAYING)
        }
        return skippingJob
    }

    fun skipToPrevious(currentTrackQueuePosition: Long): Job? {
        if (blockSkipping) return null // stopping multiple clicks
        blockSkipping = true
        skippingJob?.cancel()
//        skippingJob?.isCancelled
        skippingJob = connectionScope.launch {
            //if user skips when state is idle
            if (_customPlayerState.value == CustomPlayerState.IDLE) {
                emitPlayerState(CustomPlayerState.CHANGING_TRACK)
                blockChangingPlayerState = true
                _showVinyl.value = false
                delay(AppConst.TONEARM_MOVING_FROM_DISC_TO_START_POSITION_DURATION + 50L)
                val itemQueuePositionToBePlayed =
                    if (currentTrackQueuePosition != 0L) {
                        currentTrackQueuePosition - 1
                    } else {
                        _playingAlbum.value?.let { album ->
                            album.trackList.size - 1L
                        } ?: return@launch
                    }
                skipToQueueItem(itemQueuePositionToBePlayed)
                delay(AppConst.DELAY_AFTER_SKIPPING_BEFORE_SHOWING_VINYL) // to give more time to call all the mediaChanged calls and improve performance
                _showVinyl.value = true
                blockChangingPlayerState = false
                emitPlayerState(CustomPlayerState.IDLE)
                blockSkipping = false
                return@launch
            }
            // if item is the first at queue
            if (currentTrackQueuePosition == 0L) {
                emitPlayerState(CustomPlayerState.SEEKING_TO_BEGINNING)
                blockChangingPlayerState = true
                delay(AppConst.TONEARM_MOVING_FROM_DISC_TO_START_POSITION_DURATION + 50L)
                blockChangingPlayerState = false
                emitPlayerState(CustomPlayerState.PLAYING)
                transportControl.seekTo(0L)
                blockSkipping = false
                return@launch
            }

            // If position is above 3 seconds skipToPrevious only rewinds current track. So 2100 + 900
            // delay equals that exact duration
            val itemActuallyShouldBeChanged =
                _playbackState.value?.let { it.position < 2100 } ?: false

            if (itemActuallyShouldBeChanged) {
                emitPlayerState(CustomPlayerState.CHANGING_TRACK)
                blockChangingPlayerState = true
                mediaBrowser.sendCustomAction(ServiceConsts.PAUSE_MEDIA_PLAY_ACTION, null, null)
                _showVinyl.value = false
                delay(AppConst.TONEARM_MOVING_FROM_DISC_TO_START_POSITION_DURATION + 50L)
                skipToQueueItem(currentTrackQueuePosition - 1)
                delay(AppConst.DELAY_AFTER_SKIPPING_BEFORE_SHOWING_VINYL) // to give more time to call all the mediaChanged calls and improve performance
                _showVinyl.value = true
                blockChangingPlayerState = false
                blockSkipping = false
                delay(AppConst.SLIDE_IN_DURATION + 150L) // vinyl slides in + some additional delay
                emitPlayerState(CustomPlayerState.STARTING_AFTER_CHANGING)
                delay(100L)
                mediaBrowser.sendCustomAction(ServiceConsts.START_CRACKLE_ACTION, null, null)
                delay(AppConst.VINYL_STARTING_ANIMATION_DURATION + 500L) // vinyl starting and spins for 0.5 sec
                mediaBrowser.sendCustomAction(ServiceConsts.START_TRACK_PLAYING_ACTION, null, null)
                emitPlayerState(CustomPlayerState.PLAYING)
            } else {
                emitPlayerState(CustomPlayerState.SEEKING_TO_BEGINNING)
                blockChangingPlayerState = true
                delay(AppConst.TONEARM_MOVING_FROM_DISC_TO_START_POSITION_DURATION + 100L)
                blockChangingPlayerState = false
                blockSkipping = false
                transportControl.seekTo(0L)
                emitPlayerState(CustomPlayerState.PLAYING)
            }
        }
        return skippingJob
    }

    fun diskIsShown() {
//        if (!trackWasChanged) return
//        trackWasChanged = false
//        transportControl.play()
    }

    fun skipToQueueItem(id: Long) {
        transportControl.skipToQueueItem(id)
    }

    fun subscribe(
        parentId: String,
        callback: MediaBrowserCompat.SubscriptionCallback
    ) {
        mediaBrowser.subscribe(parentId, callback)
//        Log.e("metadata", mediaController.metadata.description.toString())
    }

    fun unsubscribe(
        parentId: String,
        callback: MediaBrowserCompat.SubscriptionCallback
    ) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    fun refreshMediaBrowserChildren() {
        mediaBrowser.sendCustomAction(ServiceConsts.REFRESH_MEDIA_PLAY_ACTION, null, null)
    }

    private var refreshCalled = false
    private fun refreshService() {
        if (refreshCalled) return
        refreshCalled = true
        connectionScope.launch {
            _shouldRefreshMusicService.emit(true)
        }
    }

    fun clearCurrentPlayingTrack() {
        _currentPlayingTrack.value = null
    }

    fun play() {
        transportControl.play()
    }

    fun stop() {
        if (this::mediaController.isInitialized) {
            transportControl.stop()
        }
    }

    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            _isConnected.value = true
        }

        override fun onConnectionSuspended() {
            _isConnected.value = false
        }

        override fun onConnectionFailed() {
            _isConnected.value = false
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)

            _playbackState.value = state
            if (state?.state == PlaybackStateCompat.STATE_NONE) refreshService()
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)

            _currentPlayingTrack.value = metadata?.let {
                playingAlbum.value?.trackList?.find { track ->
                    track.uri == metadata.description.mediaUri
                }
            }
            refreshCalled = false
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()

            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}