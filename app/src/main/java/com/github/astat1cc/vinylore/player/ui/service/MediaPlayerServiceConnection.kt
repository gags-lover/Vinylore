package com.github.astat1cc.vinylore.player.ui.service

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.github.astat1cc.vinylore.Consts
import com.github.astat1cc.vinylore.core.models.ui.AlbumUi
import com.github.astat1cc.vinylore.core.models.ui.AudioTrackUi
import com.github.astat1cc.vinylore.core.models.ui.UiState
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

    private val _isConnected = MutableStateFlow<Boolean>(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _currentPlayingTrack = MutableStateFlow<AudioTrackUi?>(null)
    val currentPlayingTrack = _currentPlayingTrack.asStateFlow()

    private val _playingAlbum = MutableStateFlow<AlbumUi?>(null)
    val playingAlbum: StateFlow<AlbumUi?> = _playingAlbum.asStateFlow()

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
        mediaBrowser.sendCustomAction(Consts.PAUSE_MEDIA_PLAY_ACTION, null, null)
        emitPlayerState(CustomPlayerState.PAUSED)
    }

    fun slowResume() {
        if (_customPlayerState.value != CustomPlayerState.PAUSED) return
        mediaBrowser.sendCustomAction(Consts.RESUME_MEDIA_PLAY_ACTION, null, null)
//        emitPlayerState(CustomPlayerState.PLAYING)
    }

    fun muteVolume() {
        mediaBrowser.sendCustomAction(Consts.MUTE, null, null)
    }

    fun unmuteVolume() {
        mediaBrowser.sendCustomAction(Consts.UNMUTE, null, null)
    }

    fun prepareMedia(album: AlbumUi) {
        emitPlayerState(CustomPlayerState.IDLE)
        _playingAlbum.value = album
        mediaBrowser.sendCustomAction(
            Consts.PREPARE_MEDIA_ACTION,
            null,
            null
        ) // todo if i need this?
        val trackToPlay = playingAlbum.value!!.trackList[0]
        transportControl.prepareFromMediaId(
            trackToPlay.uri.toString(),
            null
        )
    }

    private fun emitPlayerState(newState: CustomPlayerState) {
        val oldState = _customPlayerState.value
        if (oldState == newState ||
            (oldState == CustomPlayerState.IDLE && newState == CustomPlayerState.PAUSED)
        ) return
        _customPlayerState.value = newState
    }

    fun launchPlaying() {
        emitPlayerState(CustomPlayerState.LAUNCHING)
        connectionScope.launch {
            delay(3200L)
            mediaBrowser.sendCustomAction(Consts.START_CRACKLE_ACTION, null, null)
            delay(2500L)
            mediaBrowser.sendCustomAction(Consts.START_TRACK_PLAYING_ACTION, null, null)
            emitPlayerState(CustomPlayerState.PLAYING)
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

    fun skipToNext() {
        transportControl.skipToNext()
    }

    fun skipToPrevious() {
        transportControl.skipToPrevious()
    }

    fun skipToQueueItem(id: Long) {
        Log.e("current", "connection before ${currentPlayingTrack.value?.name}")
        transportControl.skipToQueueItem(id)
        connectionScope.launch {
            delay(3000L)
            Log.e("current", "connection after ${currentPlayingTrack.value?.name}")
        }
    }

    fun subscribe(
        parentId: String,
        callback: MediaBrowserCompat.SubscriptionCallback
    ) {
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unsubscribe(
        parentId: String,
        callback: MediaBrowserCompat.SubscriptionCallback
    ) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    fun refreshMediaBrowserChildren() {
        mediaBrowser.sendCustomAction(Consts.REFRESH_MEDIA_PLAY_ACTION, null, null)
    }

    fun clearCurrentPlayingTrack() {
        _currentPlayingTrack.value = null
        mediaController
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
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)

            _currentPlayingTrack.value = metadata?.let {
                playingAlbum.value?.trackList?.find { track ->
                    track.uri == metadata.description.mediaUri
                }
            }
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()

            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}