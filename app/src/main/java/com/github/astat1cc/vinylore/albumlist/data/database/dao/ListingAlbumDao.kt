package com.github.astat1cc.vinylore.albumlist.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.github.astat1cc.vinylore.albumlist.data.database.model.ListingAlbumDb

@Dao
interface ListingAlbumDao {

    @Query("SELECT * FROM ${ListingAlbumDb.TABLE_NAME}")
    suspend fun fetchAlbums(): List<ListingAlbumDb>

    @Insert
    suspend fun saveAlbums(albums: List<ListingAlbumDb>)

    @Query("DELETE FROM ${ListingAlbumDb.TABLE_NAME}")
    suspend fun clearAlbums()
}