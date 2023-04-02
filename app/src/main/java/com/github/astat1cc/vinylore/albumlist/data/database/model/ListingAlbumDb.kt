package com.github.astat1cc.vinylore.albumlist.data.database.model

import androidx.core.net.toUri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.astat1cc.vinylore.core.models.domain.AppListingAlbum

@Entity(tableName = ListingAlbumDb.TABLE_NAME)
data class ListingAlbumDb(
    @PrimaryKey val folderPath: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "track_count") val trackCount: Int
) {

    fun toDomain() = AppListingAlbum(folderPath.toUri(), name, trackCount)

    companion object {

        const val TABLE_NAME = "albums"

        fun fromDomain(album: AppListingAlbum) =
            with(album) { ListingAlbumDb(folderPath.toString(), name, trackCount) }
    }
}