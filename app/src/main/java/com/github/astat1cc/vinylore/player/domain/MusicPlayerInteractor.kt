package com.github.astat1cc.vinylore.player.domain

import com.github.astat1cc.vinylore.core.models.domain.AppAlbum
import com.github.astat1cc.vinylore.core.models.domain.AppAudioTrack
import kotlinx.coroutines.flow.Flow

interface MusicPlayerInteractor {

    fun fetchTrackList(): Flow<List<AppAudioTrack>>

    class Impl(
        private val repository: MusicPlayerRepository
    ) : MusicPlayerInteractor {

        override fun fetchTrackList(): Flow<List<AppAudioTrack>> = repository.fetchAlbum()
    }
}