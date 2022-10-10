package com.github.astat1cc.vinylore.tracklist.di

import android.content.Context
import androidx.room.RoomDatabase
import com.github.astat1cc.vinylore.core.AppResourceProvider
import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.ErrorHandler
import com.github.astat1cc.vinylore.core.database.AppDatabase
import com.github.astat1cc.vinylore.tracklist.data.TrackListRepositoryImpl
import com.github.astat1cc.vinylore.tracklist.domain.TrackListInteractor
import com.github.astat1cc.vinylore.tracklist.domain.TrackListRepository
import com.github.astat1cc.vinylore.tracklist.ui.AppFileProvider
import com.github.astat1cc.vinylore.tracklist.ui.TrackListViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

const val SHARED_PREFS_NAME = "APP_SHARED_PREFS"

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
    single<TrackListRepository> {
        TrackListRepositoryImpl(sharedPrefs = get(), fileProvider = get(), dispatchers = get())
    }
    single<AppResourceProvider> {
        AppResourceProvider.Impl(androidContext())
    }
    single<ErrorHandler> {
        ErrorHandler.Impl(resources = get())
    }
    single<TrackListInteractor> {
        TrackListInteractor.Impl(repository = get(), errorHandler = get(), dispatchers = get())
    }
    single<DispatchersProvider> {
        DispatchersProvider.Impl()
    }
    viewModel {
        TrackListViewModel(interactor = get(), dispatchers = get(), errorHandler = get())
    }
}

fun provideTrackListDao(database: AppDatabase) = database.trackListDao()
