package com.github.astat1cc.vinylore.albumlist.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.astat1cc.vinylore.albumlist.data.database.dao.ListingAlbumDao
import com.github.astat1cc.vinylore.albumlist.data.database.model.ListingAlbumDb

@Database(
    entities = [ListingAlbumDb::class],
    version = 1
)
abstract class AlbumsDatabase : RoomDatabase() {

    abstract fun albumsDao(): ListingAlbumDao

    companion object {

        const val DATABASE_NAME = "albums_database"
    }
}