package com.github.astat1cc.vinylore.player.data

import com.github.astat1cc.vinylore.core.models.domain.AppAudioTrack
import com.github.astat1cc.vinylore.player.data.dao.PlayerDao
import com.github.astat1cc.vinylore.player.domain.MusicPlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicPlayerRepositoryImpl(
    private val dao: PlayerDao
) : MusicPlayerRepository {

    override fun fetchAlbum(): Flow<List<AppAudioTrack>> =
        dao.fetchTrackList().map { trackListDb ->
            trackListDb.map { trackDb -> trackDb.toDomain() }
        }
}