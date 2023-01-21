package com.github.astat1cc.vinylore.player.ui.service

import android.app.PendingIntent
import android.content.Intent
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.media.MediaBrowserServiceCompat
import com.github.astat1cc.vinylore.Consts
import com.github.astat1cc.vinylore.Consts.MY_MEDIA_ROOT_ID
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class MusicService : MediaBrowserServiceCompat() {

    private val dataSourceFactory by inject<CacheDataSource.Factory>()

    private val trackExoPlayer by inject<ExoPlayer>()
    private val crackleExoPLayer by inject<ExoPlayer>()

    private val mediaSource by inject<MusicMediaSource>()

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private lateinit var musicNotificationManager: MusicNotificationManager
    var currentPlayingMedia: MediaMetadataCompat? = null
    private var isPlayerInitialized = false
    var isForegroundService = false

    companion object {
        var curSongDuration = 0L
            private set

        private const val TAG = "MusicService"
    }

    override fun onCreate() {
        super.onCreate()

        val sessionActivityIntent = packageManager
            ?.getLaunchIntentForPackage(packageName)
            ?.let { sessionIntent ->
                PendingIntent.getActivity(
                    this,
                    0,
                    sessionIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE
                )
            }

        mediaSession = MediaSessionCompat(this, TAG).apply {
            setSessionActivity(sessionActivityIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        musicNotificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        )

        loadSource()

        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
            setPlaybackPreparer(AudioMediaPlayBackPreparer())
            setQueueNavigator(MediaQueueNavigator(mediaSource, mediaSession))
            setPlayer(trackExoPlayer)
        }

        musicNotificationManager.showNotification(trackExoPlayer)
    }

    private fun loadSource() {
        serviceScope.launch {
            mediaSource.load()
        }
    }

    var sync = false
    fun startPlayersSync() {
        sync = true
        serviceScope.launch {
            while (sync) {
                crackleExoPLayer.playWhenReady = trackExoPlayer.playWhenReady
                delay(300L)
            }
        }
    }

    fun stopPlayerSync() {
        sync = false
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceScope.cancel()
        trackExoPlayer.release()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot = BrowserRoot(MY_MEDIA_ROOT_ID, null)

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {

        when (parentId) {
            MY_MEDIA_ROOT_ID -> {
                val resultsSent = mediaSource.whenReady { isReady ->
                    if (isReady) {
                        result.sendResult(mediaSource.asMediaItem())
                    } else {
                        result.sendResult(null)
                    }
                }

                if (!resultsSent) {
                    result.detach()
                }
            }
            else -> Unit
        }
    }

    override fun onCustomAction(action: String, extras: Bundle?, result: Result<Bundle>) {
        super.onCustomAction(action, extras, result)

        when (action) {
            Consts.START_CRACKLE_ACTION -> startCrackle()
            Consts.START_TRACK_PLAYING_ACTION -> startTrackPlaying()
            Consts.RESUME_MEDIA_PLAY_ACTION -> slowlyResume()
            Consts.PAUSE_MEDIA_PLAY_ACTION -> slowlyPause()
            Consts.PREPARE_MEDIA_ACTION -> musicNotificationManager.showNotification(trackExoPlayer)
            Consts.REFRESH_MEDIA_PLAY_ACTION -> {
                mediaSource.refresh()
                notifyChildrenChanged(MY_MEDIA_ROOT_ID)
            }
            else -> Unit
        }
    }

    private fun startTrackPlaying() {
        startPlayersSync()
        trackExoPlayer.playWhenReady = true
    }

    private fun startCrackle() {
        stopPlayerSync()
        crackleExoPLayer.playWhenReady = true
    }

    private fun slowlyPause() {
        serviceScope.launch {
            var currentSpeed = 1f
            while (currentSpeed > 0.11) {
                currentSpeed -= 0.1f
                trackExoPlayer.playbackParameters =
                    PlaybackParameters(
                        currentSpeed,
                        0.8f + currentSpeed / 10
                    )
                trackExoPlayer.volume = currentSpeed * 1.2f
                delay(100L)
            }
            trackExoPlayer.playWhenReady = false
            crackleExoPLayer.playWhenReady = false

            // because it seems that app saves params even after closing app
            trackExoPlayer.playbackParameters = PlaybackParameters(1f, 1f)
            trackExoPlayer.volume = 1f
        }
    }

    private fun slowlyResume() {
        serviceScope.launch {
            crackleExoPLayer.playWhenReady = true
            trackExoPlayer.playWhenReady = true
            var currentSpeed = 0.1f
            while (currentSpeed < 1) {
                currentSpeed += 0.1f
                trackExoPlayer.playbackParameters =
                    PlaybackParameters(
                        currentSpeed,
                        0.8f + currentSpeed / 10
                    )
                trackExoPlayer.volume = currentSpeed * 2f
                delay(100L)
            }
            trackExoPlayer.playbackParameters = PlaybackParameters(1f, 1f)
            trackExoPlayer.volume = 1f
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        with(trackExoPlayer) {
            stop()
            clearMediaItems()
        }
        with(crackleExoPLayer) {
            stop()
            clearMediaItems()
        }
    }

    private inner class AudioMediaPlayBackPreparer : MediaSessionConnector.PlaybackPreparer {

        override fun onCommand(
            player: Player,
            command: String,
            extras: Bundle?,
            cb: ResultReceiver?
        ) = false

        override fun getSupportedPrepareActions(): Long =
            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID

        override fun onPrepare(playWhenReady: Boolean) = Unit

        override fun onPrepareFromMediaId(
            mediaId: String,
            playWhenReady: Boolean,
            extras: Bundle?
        ) {
            mediaSource.whenReady {

                val itemToPlay = mediaSource.audioMediaMetadata.find {
                    it.description.mediaId == mediaId
                }

                currentPlayingMedia = itemToPlay

                preparePlayer(
//                    mediaMetadata = mediaSource.audioMediaMetadata,
//                    itemToPlay = itemToPlay,
//                    playWhenReady = false
                )
            }
        }

        override fun onPrepareFromSearch(
            query: String,
            playWhenReady: Boolean,
            extras: Bundle?
        ) = Unit

        override fun onPrepareFromUri(
            uri: Uri,
            playWhenReady: Boolean,
            extras: Bundle?
        ) = Unit

        private fun preparePlayer(
//            mediaMetadata: List<MediaMetadataCompat>,
//            itemToPlay: MediaMetadataCompat?,
//            playWhenReady: Boolean
        ) {
//            val indexToPlay = if (currentPlayingMedia == null) {
//                0
//            } else {
//                mediaMetadata.indexOf(itemToPlay)
//            }
            with(serviceScope) {
                launch {
                    with(trackExoPlayer) {
                        addListener(PlayerEventListener())
                        setMediaSource(mediaSource.trackMediaSource(dataSourceFactory))
                        prepare()
                        this.playWhenReady = false
                    }
                }
                launch {
                    with(crackleExoPLayer) {
                        addListener(PlayerEventListener())
                        setMediaSource(mediaSource.crackleMediaSource(dataSourceFactory))
                        repeatMode = Player.REPEAT_MODE_ONE
                        prepare()
                        this.playWhenReady = false
                    }
                }
            }
        }

        private inner class PlayerEventListener : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING, Player.STATE_READY -> {
                        musicNotificationManager.showNotification(trackExoPlayer)
                    }
                    else -> musicNotificationManager.hideNotification()
                }
            }

            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)

                curSongDuration = player.duration
            }

            override fun onPlayerError(error: PlaybackException) {
                throw error
                var message = error.message
//                var message = R.string.generic_error

//                if (error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND) {
//                    message = R.string.error_media_not_found
//                } todo

                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}