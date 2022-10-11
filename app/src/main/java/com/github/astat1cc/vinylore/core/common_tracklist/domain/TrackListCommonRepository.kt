package com.github.astat1cc.vinylore.core.common_tracklist.domain

import com.github.astat1cc.vinylore.core.models.domain.AppAlbum

interface TrackListCommonRepository {

    suspend fun fetchAlbums(): List<AppAlbum>?
}