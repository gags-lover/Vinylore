package com.github.astat1cc.vinylore.player.di

import com.github.astat1cc.vinylore.core.database.AppDatabase
import com.github.astat1cc.vinylore.player.data.MusicPlayerRepositoryImpl
import com.github.astat1cc.vinylore.player.domain.MusicPlayerInteractor
import com.github.astat1cc.vinylore.player.domain.MusicPlayerRepository
import com.github.astat1cc.vinylore.player.ui.AudioViewModel
import com.github.astat1cc.vinylore.player.ui.service.MediaPlayerServiceConnection
import com.github.astat1cc.vinylore.player.ui.service.MusicMediaSource
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.ParametersBuilder
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.io.File


val playerModule = module {
    single {
        providePlayerDao(database = get())
    }
    single {
        AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
    }
    factory {
        val defaultRenderersFactory =
            DefaultRenderersFactory(androidContext()).setEnableAudioTrackPlaybackParams(true)
        ExoPlayer.Builder(androidContext(), defaultRenderersFactory)
            .build()
            .apply {
                setAudioAttributes(get(), false)
                setHandleAudioBecomingNoisy(true)
            }
    }
    single<DataSource.Factory> {
        DefaultDataSource.Factory(androidContext())
    }
    single<CacheDataSource.Factory> {
        val cacheDir = File(androidContext().cacheDir, "media")
        val databaseProvider = StandaloneDatabaseProvider(androidContext())
        val cache = SimpleCache(cacheDir, NoOpCacheEvictor(), databaseProvider)
        CacheDataSource.Factory().apply {
            setCache(cache)
            setUpstreamDataSourceFactory(get())
        }
    }
    single {
        MusicMediaSource(interactor = get())
    }
    single {
        MediaPlayerServiceConnection(androidContext())
    }
    single<MusicPlayerRepository> {
        MusicPlayerRepositoryImpl(sharedPrefs = get())
    }
    single<MusicPlayerInteractor> {
        MusicPlayerInteractor.Impl(
            playerRepository = get(),
            trackListRepository = get(),
            dispatchers = get(),
            errorHandler = get()
        )
    }
    viewModel {
        AudioViewModel(
            interactor = get(),
            serviceConnection = get(),
            errorHandler = get()
        )
    }
}

fun providePlayerDao(database: AppDatabase) = database.playerDao()