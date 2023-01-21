package com.github.astat1cc.vinylore.albumlist.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.github.astat1cc.vinylore.core.models.data.AudioTrackDb

// todo delete this and database as well
@Dao
interface AlbumListDao {

    @Insert(entity = AudioTrackDb::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAlbums(albums: List<AudioTrackDb>)
}