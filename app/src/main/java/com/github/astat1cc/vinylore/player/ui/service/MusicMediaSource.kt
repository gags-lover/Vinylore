package com.github.astat1cc.vinylore.player.ui.service

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.models.domain.FetchResult
import com.github.astat1cc.vinylore.core.models.ui.AudioTrackUi
import com.github.astat1cc.vinylore.player.domain.MusicPlayerInteractor
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

typealias OnReadyListener = (Boolean) -> Unit

class MusicMediaSource(
    private val interactor: MusicPlayerInteractor,
) {

    private val onReadyListeners: MutableList<OnReadyListener> = mutableListOf()

    var audioMediaMetadata: List<MediaMetadataCompat> = emptyList()

    private var state: AudioSourceState = AudioSourceState.CREATED
        set(value) {
//            if (value == AudioSourceState.CREATED || value == AudioSourceState.ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    executeCollectedListeners()
                }
//            } else {
//                field = value
//            }
        }

    private fun executeCollectedListeners() {
        onReadyListeners.forEach { listener ->
            listener(isReady)
        }
        onReadyListeners.clear()
    }

    suspend fun load() {
        state = AudioSourceState.INITIALIZING

        withContext(Dispatchers.IO) {
            interactor.fetchTrackList().collect { fetchResult ->
                if (fetchResult !is FetchResult.Success || fetchResult.data == null) return@collect
                val trackListUi =
                    fetchResult.data.trackList.map { trackDomain -> AudioTrackUi.fromDomain(trackDomain) }
                audioMediaMetadata = trackListUi.map { track ->
                    MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, track.uri.toString())
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, track.uri.toString())
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.name)
                        .build()
                }
                state = AudioSourceState.INITIALIZED
            }
//            repository.fetchAlbums()?.let { // todo
//                val data = it.map { albumDomain ->
//                    albumDomain.trackList.map { track ->
//                        AudioTrackUi.fromDomain(track)
//                    }
//                }.flatten()
        }
    }

    fun crackleMediaSource(dataSourceFactory: CacheDataSource.Factory): ProgressiveMediaSource {
        val cracklingUri = RawResourceDataSource.buildRawResourceUri(R.raw.vinyl_crackle_test)
        val cracklingItem = MediaItem.fromUri(cracklingUri)
        return ProgressiveMediaSource
            .Factory(dataSourceFactory)
            .createMediaSource(cracklingItem)
    }

    fun trackMediaSource(
        dataSourceFactory: CacheDataSource.Factory
    ): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()

        audioMediaMetadata.forEach { mediaMetadata ->
            val mediaItem = MediaItem.fromUri(
                mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)
            )

            val mediaSource = ProgressiveMediaSource
                .Factory(dataSourceFactory)
                .createMediaSource(mediaItem)


            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asMediaItem(): MutableList<MediaBrowserCompat.MediaItem> =
        audioMediaMetadata.map { metadata ->
            val description = MediaDescriptionCompat.Builder()
                .setTitle(metadata.description.title)
                .setMediaUri(metadata.description.mediaUri)
                .setMediaId(metadata.description.mediaId)
                .build()
            MediaBrowserCompat.MediaItem(description, FLAG_PLAYABLE)
        }.toMutableList()

    fun refresh() {
        onReadyListeners.clear()
        state = AudioSourceState.CREATED
    }

    fun whenReady(listener: OnReadyListener): Boolean =
        if (state == AudioSourceState.CREATED || state == AudioSourceState.INITIALIZING) {
            onReadyListeners += listener
            false
        } else {
            listener(isReady)
            true
        }

    private val isReady: Boolean
        get() = state == AudioSourceState.INITIALIZED
}

enum class AudioSourceState {
    CREATED,
    INITIALIZING,
    INITIALIZED,
    ERROR
}