package com.github.astat1cc.vinylore.player.domain

import com.github.astat1cc.vinylore.core.models.domain.AppAudioTrack
import kotlinx.coroutines.flow.Flow

interface MusicPlayerRepository {

    fun fetchAlbum(): Flow<List<AppAudioTrack>>
}