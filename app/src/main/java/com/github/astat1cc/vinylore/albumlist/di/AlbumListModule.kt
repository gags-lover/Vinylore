package com.github.astat1cc.vinylore.albumlist.di

import android.content.Context
import androidx.room.Room
import com.github.astat1cc.vinylore.albumlist.data.AlbumListScreenRepositoryImpl
import com.github.astat1cc.vinylore.albumlist.data.database.AlbumsDatabase
import com.github.astat1cc.vinylore.albumlist.data.database.dao.ListingAlbumDao
import com.github.astat1cc.vinylore.albumlist.data.database.model.ListingAlbumDb
import com.github.astat1cc.vinylore.albumlist.domain.AlbumListScreenInteractor
import com.github.astat1cc.vinylore.albumlist.domain.AlbumListScreenRepository
import com.github.astat1cc.vinylore.albumlist.ui.AlbumListScreenViewModel
import com.github.astat1cc.vinylore.core.AppErrorHandler
import com.github.astat1cc.vinylore.core.AppResourceProvider
import com.github.astat1cc.vinylore.core.DispatchersProvider
import com.github.astat1cc.vinylore.core.common_tracklist.data.AppFileProvider
import com.github.astat1cc.vinylore.core.common_tracklist.data.CommonRepositoryImpl
import com.github.astat1cc.vinylore.core.common_tracklist.domain.CommonRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

const val SHARED_PREFS_NAME = "APP_SHARED_PREFS" // todo replace to app const

val albumListModule = module {
    single {
        androidContext().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }
    single<AppFileProvider> {
        AppFileProvider.Impl(androidContext())
    }
    single<CommonRepository> {
        CommonRepositoryImpl(
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
    single<AlbumsDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AlbumsDatabase::class.java,
            AlbumsDatabase.DATABASE_NAME
        ).build()
    }
    single<AlbumListScreenRepository> {
        AlbumListScreenRepositoryImpl(
            sharedPrefs = get(),
            dispatchers = get(),
            fileProvider = get(),
            albumsDao = provideAlbumsDao(database = get())
        )
    }
    single<AlbumListScreenInteractor> {
        AlbumListScreenInteractor.Impl(
            dispatchers = get(),
            albumListScreenRepository = get(),
            errorHandler = get()
        )
    }
    viewModel {
        AlbumListScreenViewModel(
            interactor = get(),
            dispatchers = get(),
            errorHandler = get(),
            serviceConnection = get()
        )
    }
}

private fun provideAlbumsDao(database: AlbumsDatabase): ListingAlbumDao = database.albumsDao()
