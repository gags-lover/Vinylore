package com.github.astat1cc.vinylore.core.models.data

import androidx.room.Entity
import com.github.astat1cc.vinylore.core.models.data.AlbumDb.Companion.TABLE_NAME

@Entity(tableName = TABLE_NAME)
class AlbumDb(
    val id: Int,
    val name: String,
    val trackList: List<AudioTrackDb>
) {

    companion object {

        const val TABLE_NAME = "albums"
    }
}