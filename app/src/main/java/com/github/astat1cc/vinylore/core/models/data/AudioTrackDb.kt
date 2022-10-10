package com.github.astat1cc.vinylore.core.models.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.astat1cc.vinylore.core.models.data.AudioTrackDb.Companion.TABLE_NAME
import com.github.astat1cc.vinylore.core.models.domain.AppAudioTrack

@Entity(tableName = TABLE_NAME)
data class AudioTrackDb(
    @PrimaryKey val uri: String,
    val name: String?
) {

    fun toDomain() = AppAudioTrack(uri, name ?: "No name") // todo remove hardcode

    companion object {

        const val TABLE_NAME = "tracks"
    }
}