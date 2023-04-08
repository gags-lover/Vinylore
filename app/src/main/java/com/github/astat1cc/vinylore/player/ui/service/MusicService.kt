package com.github.astat1cc.vinylore.player.ui.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.media.MediaBrowserServiceCompat
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.ServiceConsts
import com.github.astat1cc.vinylore.ServiceConsts.MY_MEDIA_ROOT_ID
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.android.ext.android.inject
import java.io.File

// todo when service is killed, app is not i need to launch service again

private const val CRACKLE_STANDARD_VOLUME = 0.75f
private const val TRACK_STANDARD_VOLUME = 1f

class MusicService : MediaBrowserServiceCompat() {

    private val dataSourceFactory by inject<CacheDataSource.Factory>()

    private val trackExoPlayer by inject<ExoPlayer>()
    private val crackleExoPlayer by inject<ExoPlayer>()

    private val mediaSource by inject<MusicMediaSource>()

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private lateinit var musicNotificationManager: MusicNotificationManager
    var currentPlayingMedia: MediaMetadataCompat? = null
    private var isPlayerInitialized = false
    var isForegroundService = false
    private var errorOccurred = false

    companion object {

        // todo do normal way
        private val _curSongDuration = MutableStateFlow(0L)
        val curSongDuration = _curSongDuration.asStateFlow()

        private val _taskWasRemoved = MutableStateFlow(false)
        val taskWasRemoved: StateFlow<Boolean> = _taskWasRemoved.asStateFlow()

        fun taskRestored() {
            _taskWasRemoved.value = false
        }

        private const val TAG = "MusicService"
    }

    override fun onCreate() {
        super.onCreate()

        Log.e("meta", "oncreate")

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

        startPlayersSync()
    }

    private fun loadSource() {
        serviceScope.launch {
            mediaSource.load()
        }
    }

    var sync = false
    private fun startPlayersSync() {
        sync = true
        serviceScope.launch {
            while (sync) {
                crackleExoPlayer.playWhenReady = trackExoPlayer.playWhenReady
                delay(300L)
            }
        }
    }

    private fun stopPlayerSync() {
        sync = false
    }

    fun stopCrackle() {
//        stopPlayerSync()
        trackExoPlayer.playWhenReady = false
        crackleExoPlayer.playWhenReady = false
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
            ServiceConsts.START_CRACKLE_ACTION -> startCrackle()
            ServiceConsts.START_TRACK_PLAYING_ACTION -> startTrackPlaying()
            ServiceConsts.SLOWLY_RESUME_MEDIA_PLAY_ACTION -> slowlyResume()
            ServiceConsts.SLOWLY_PAUSE_MEDIA_PLAY_ACTION -> slowlyPause()
            ServiceConsts.PREPARE_MEDIA_ACTION -> musicNotificationManager.showNotification(
                trackExoPlayer
            )
            ServiceConsts.REFRESH_MEDIA_PLAY_ACTION -> {
                mediaSource.refresh()
                notifyChildrenChanged(MY_MEDIA_ROOT_ID)
            }
            ServiceConsts.MUTE -> mute()
            ServiceConsts.UNMUTE -> unmute()
            ServiceConsts.PAUSE_MEDIA_PLAY_ACTION -> pause()
            else -> Unit
        }
    }

    private fun mute() {
        trackExoPlayer.volume = 0f
        crackleExoPlayer.volume = 0f
    }

    private fun unmute() {
        serviceScope.launch {
            // delay to escape cases where seeking is not completed and user hears prev position
            // sound for some time
            delay(50L)
            trackExoPlayer.volume = TRACK_STANDARD_VOLUME
            crackleExoPlayer.volume = CRACKLE_STANDARD_VOLUME
        }
    }

    private fun startTrackPlaying() {
        if (errorOccurred) return
        startPlayersSync()
        trackExoPlayer.playWhenReady = true
    }

    private fun startCrackle() {
        if (errorOccurred) return // that means player error occurred
        stopPlayerSync()
        crackleExoPlayer.playWhenReady = true
    }

    private var shouldSlowlyPause = false
    private fun slowlyPause() {
        shouldSlowlyResume = false
        shouldSlowlyPause = true
        serviceScope.launch {
            var currentSpeed = 1f
            while (currentSpeed > 0.11 && shouldSlowlyPause) {
                currentSpeed -= 0.09f
                trackExoPlayer.playbackParameters =
                    PlaybackParameters(
                        currentSpeed,
                        0.6f + currentSpeed / 2.5f
                    )
                trackExoPlayer.volume = currentSpeed
                crackleExoPlayer.volume = currentSpeed
                delay(100L)
            }
            trackExoPlayer.playWhenReady = false
//            crackleExoPlayer.playWhenReady = false

            // because it seems that app saves params even after closing app
            trackExoPlayer.playbackParameters = PlaybackParameters(1f, 1f)
            trackExoPlayer.volume = TRACK_STANDARD_VOLUME
            crackleExoPlayer.volume = CRACKLE_STANDARD_VOLUME
        }
    }

    private var shouldSlowlyResume = false
    private fun slowlyResume() {
        if (errorOccurred) return
        shouldSlowlyResume = true
        shouldSlowlyPause = false
        serviceScope.launch {
            launch {
                delay(50L)
//                crackleExoPlayer.playWhenReady = true
                trackExoPlayer.playWhenReady = true
            }
            var currentSpeed = 0.1f
            while (currentSpeed < 1 && shouldSlowlyResume) {
                currentSpeed += 0.09f
                trackExoPlayer.playbackParameters =
                    PlaybackParameters(
                        currentSpeed,
                        0.6f + currentSpeed / 2.5f
                    )
                trackExoPlayer.volume = currentSpeed
                crackleExoPlayer.volume = currentSpeed
                delay(100L)
            }
            trackExoPlayer.playbackParameters = PlaybackParameters(1f, 1f)
            trackExoPlayer.volume = TRACK_STANDARD_VOLUME
            crackleExoPlayer.volume = CRACKLE_STANDARD_VOLUME
        }
    }

    private fun pause() {
        crackleExoPlayer.playWhenReady = false
        trackExoPlayer.playWhenReady = false
        startPlayersSync()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        _taskWasRemoved.value = true

//        with(trackExoPlayer) {
//            stop()
//            clearMediaItems()
//        }
//        with(crackleExoPlayer) {
//            stop()
//            clearMediaItems()
//        }
    }

    fun clearCacheAndStopService() {
        serviceScope.launch(Dispatchers.IO) {
            deleteCache(this@MusicService)
            stopSelf()
        }
    }

    private fun deleteCache(context: Context) {
        try {
            val dir: File = context.getCacheDir()
            val result = deleteDir(dir)
            Log.e("cache", "deleted $result")
        } catch (e: Exception) {
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory()) {
            val children: Array<String> = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
            dir.delete()
        } else if (dir != null && dir.isFile()) {
            dir.delete()
        } else {
            false
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
                        repeatMode = Player.REPEAT_MODE_ALL
                        prepare()
                        this.playWhenReady = false
                        volume = TRACK_STANDARD_VOLUME
                    }
                }
                launch {
                    with(crackleExoPlayer) {
//                        addListener(PlayerEventListener())
                        setMediaSource(mediaSource.crackleMediaSource(dataSourceFactory))
                        repeatMode = Player.REPEAT_MODE_ONE
                        prepare()
                        this.playWhenReady = false
                        volume = CRACKLE_STANDARD_VOLUME
                    }
                }
            }
        }

        private fun tryEmitDuration(newDuration: Long) {
            val oldDuration = curSongDuration.value
            if (oldDuration == newDuration) return
            _curSongDuration.value = newDuration
        }

        private inner class PlayerEventListener : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING, Player.STATE_READY -> {
                        errorOccurred = false
                        musicNotificationManager.showNotification(trackExoPlayer)
                    }
                    else -> {
                        musicNotificationManager.hideNotification()
                        stopCrackle()
                    }
                }
            }

            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)

                tryEmitDuration(player.duration)
//                Log.e("metadata","onEvents in MusicService ${player.currentMediaItem}")
//                tryEmitCurrentPlayingTrack(player.currentMediaItem?.mediaId)
            }

            override fun onPlayerError(error: PlaybackException) {
                errorOccurred = true
                val message =
                    if (error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND) {
                        getString(R.string.error_media_not_found)
                    } else {
                        error.message
                    }

                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
            }
        }
    }
}