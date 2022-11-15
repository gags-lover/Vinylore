package com.github.astat1cc.vinylore.tracklist.di

import com.github.astat1cc.vinylore.tracklist.ui.TrackListScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val trackListModule = module {
    viewModel {
        TrackListScreenViewModel(errorHandler = get(), serviceConnection = get())
    }
}