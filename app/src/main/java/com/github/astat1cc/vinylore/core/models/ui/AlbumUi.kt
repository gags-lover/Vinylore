package com.github.astat1cc.vinylore.core.models.ui

import com.github.astat1cc.vinylore.core.models.domain.AppAlbum

data class AlbumUi(
    val id: Int,
    val name: String?,
    val trackList: List<AudioTrackUi>
) {

//    fun toDomain() = AppAlbum(
//        name ?: "No name",
//        trackList.map { trackUi -> trackUi.toDomain() },
//        id
//    ) // todo remove hardcoded name

    companion object {

        fun fromDomain(album: AppAlbum) = with(album) {
            AlbumUi(
                id,
                name,
                trackList.map { albumDomain -> AudioTrackUi.fromDomain(albumDomain) }
            )
        }
    }
}