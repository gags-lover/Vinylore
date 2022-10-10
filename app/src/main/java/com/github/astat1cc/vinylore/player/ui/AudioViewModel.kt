package com.github.astat1cc.vinylore.player.ui

import android.support.v4.media.MediaBrowserCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.astat1cc.vinylore.Consts
import com.github.astat1cc.vinylore.player.ui.service.MediaPlayerServiceConnection
import com.github.astat1cc.vinylore.player.ui.service.MusicService
import com.github.astat1cc.vinylore.player.ui.service.currentPosition
import com.github.astat1cc.vinylore.player.ui.service.isPlaying
import com.github.astat1cc.vinylore.core.models.ui.AudioTrackUi
import com.github.astat1cc.vinylore.player.domain.MusicPlayerInteractor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AudioViewModel(
    private val interactor: MusicPlayerInteractor,
    serviceConnection: MediaPlayerServiceConnection,
) : ViewModel() {

    var trackList = interactor.fetchTrackList()
        .map { trackListDomain ->
            trackListDomain.map { trackDomain -> AudioTrackUi.fromDomain(trackDomain) }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    var currentPlayingAudio = serviceConnection.currentPlayingAudio
    private val isConnected = serviceConnection.isConnected
    lateinit var rootMediaId: String
    var currentPlaybackPosition by mutableStateOf(0L)
    private var updatePosition = true
    private val playbackState = serviceConnection.playbackState
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

    init {
        viewModelScope.launch {
//            albumList += fetchAudioTracksNotFlow()
            isConnected.collect {
                if (it) {
                    rootMediaId = serviceConnection.rootMediaId
                    serviceConnection.playbackState.value?.apply {
                        currentPlaybackPosition = position
                    }
                    serviceConnection.subscribe(rootMediaId, subscriptionCallback)
                }
            }
        }
    }

    fun playAudio(currentAudio: AudioTrackUi) {
        serviceConnection.playAudio(trackList.value)
        if (currentAudio.uri == currentPlayingAudio.value?.uri) {
            if (isAudioPlaying) {
                serviceConnection.transportControl.pause()
            } else {
                serviceConnection.transportControl.play()
            }
        } else {
            serviceConnection.transportControl.playFromMediaId(
                currentAudio.uri.toString(),
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
}