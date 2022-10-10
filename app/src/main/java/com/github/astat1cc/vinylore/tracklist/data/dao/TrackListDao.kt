package com.github.astat1cc.vinylore.tracklist.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.github.astat1cc.vinylore.core.models.data.AudioTrackDb

@Dao
interface TrackListDao {

    @Insert(entity = AudioTrackDb::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTrackList(trackList: List<AudioTrackDb>)
}