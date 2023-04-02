package com.github.astat1cc.vinylore.core.common_tracklist.domain

import com.github.astat1cc.vinylore.core.models.domain.AppListingAlbum
import com.github.astat1cc.vinylore.core.models.domain.AppPlayingAlbum

interface CommonRepository {

    suspend fun fetchPlayingAlbum(): AppPlayingAlbum?
}