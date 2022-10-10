package com.github.astat1cc.vinylore

import android.app.Application
import com.github.astat1cc.vinylore.core.database.databaseModule
import com.github.astat1cc.vinylore.player.di.playerModule
import com.github.astat1cc.vinylore.tracklist.di.trackListModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class VinyloreApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@VinyloreApp)
            modules(databaseModule, trackListModule, playerModule)
        }
    }
}