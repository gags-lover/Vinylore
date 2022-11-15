package com.github.astat1cc.vinylore.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.astat1cc.vinylore.core.models.data.AudioTrackDb
import com.github.astat1cc.vinylore.player.data.dao.PlayerDao
import com.github.astat1cc.vinylore.albumlist.data.dao.AlbumListDao

@Database(
    entities = [AudioTrackDb::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun trackListDao(): AlbumListDao

    abstract fun playerDao(): PlayerDao

    companion object {

        const val DATABASE_NAME = "app_database"
    }
}