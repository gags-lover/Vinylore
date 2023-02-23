package com.github.astat1cc.vinylore.core.common_tracklist.domain

import com.github.astat1cc.vinylore.core.models.domain.AppAlbum

interface CommonRepository {

    suspend fun fetchAlbums(refresh: Boolean): List<AppAlbum>?
}