package com.github.astat1cc.vinylore.player.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.github.astat1cc.vinylore.core.models.data.AudioTrackDb
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Query("SELECT * FROM ${AudioTrackDb.TABLE_NAME}")
    fun fetchTrackList(): Flow<List<AudioTrackDb>>
}