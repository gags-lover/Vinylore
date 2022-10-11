package com.github.astat1cc.vinylore.tracklist.di

import android.content.Context
import com.github.astat1cc.vinylore.core.AppResourceProvider
import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.database.AppDatabase
import com.github.astat1cc.vinylore.core.common_tracklist.data.TrackListCommonRepositoryImpl
import com.github.astat1cc.vinylore.core.common_tracklist.domain.TrackListCommonRepository
import com.github.astat1cc.vinylore.tracklist.data.TrackListScreenRepositoryImpl
import com.github.astat1cc.vinylore.tracklist.domain.TrackListScreenInteractor
import com.github.astat1cc.vinylore.tracklist.domain.TrackListScreenRepository
import com.github.astat1cc.vinylore.tracklist.data.AppFileProvider
import com.github.astat1cc.vinylore.tracklist.ui.TrackListScreenViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

const val SHARED_PREFS_NAME = "APP_SHARED_PREFS" // todo replace to app const

val trackListModule = module {
    single {
        provideTrackListDao(database = get())
    }
    single {
        androidContext().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }
    single<AppFileProvider> {
        AppFileProvider.Impl(androidContext())
    }
    single<TrackListCommonRepository> {
        TrackListCommonRepositoryImpl(
            sharedPrefs = get(),
            fileProvider = get(),
            dispatchers = get()
        )
    }
    single<AppResourceProvider> {
        AppResourceProvider.Impl(androidContext())
    }
    single<AppErrorHandler> {
        AppErrorHandler.Impl(resources = get())
    }
    single<DispatchersProvider> {
        DispatchersProvider.Impl()
    }
    single<TrackListScreenRepository> {
        TrackListScreenRepositoryImpl(sharedPrefs = get())
    }
    single<TrackListScreenInteractor> {
        TrackListScreenInteractor.Impl(
            dispatchers = get(),
            trackListCommonRepository = get(),
            trackListScreenRepository = get(),
            errorHandler = get()
        )
    }
    viewModel {
        TrackListScreenViewModel(
            interactor = get(),
            dispatchers = get(),
            errorHandler = get()
        )
    }
} // todo replace common to core

fun provideTrackListDao(database: AppDatabase) = database.trackListDao()
