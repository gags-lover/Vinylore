package com.github.astat1cc.vinylore.core.common_tracklist.data

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.models.domain.AppPlayingAlbum
import com.github.astat1cc.vinylore.core.models.domain.AppAudioTrack
import com.github.astat1cc.vinylore.core.models.domain.AppListingAlbum
import com.github.astat1cc.vinylore.core.util.removeUnderscoresAndPathSegment

// todo make try here to avoid crashes

interface AppFileProvider {

//    suspend fun getAlbumListFrom(uri: Uri): List<AppPlayingAlbum>// todo delete

    suspend fun getOnlyAlbumListFrom(uri: Uri): List<AppListingAlbum>

    suspend fun getAlbumFrom(uri: Uri): AppPlayingAlbum

    class Impl(
        private val context: Context
    ) : AppFileProvider {

//        override suspend fun getAlbumListFrom(uri: Uri): List<AppPlayingAlbum> {
//            val chosenDir = DocumentFile.fromTreeUri(
//                context,
//                uri
//            )!! // always non-null because returns null only if sdk < 21
//            val albumWrapperList = chosenDir.getInnerStoredAlbums() +
//                    AlbumForPlayingDataWrapper(
//                        chosenDir.name ?: getDefaultName(), chosenDir.getTrackList()
//                    )
//
//            return albumWrapperList
//                .filter { albumWrapper -> albumWrapper.trackList.isNotEmpty() }
//                .map { albumWrapper ->
//                    val firstTrackArtist = albumWrapper.trackList[0].artist
//                    AppPlayingAlbum(
//                        folderPath = albumWrapper.hashCode(),
//                        name = albumWrapper.name,
//                        trackList = albumWrapper.trackList,
//                        albumOfOneArtist = if (firstTrackArtist != null) {
//                            albumWrapper.trackList.all { track ->
//                                track.artist == firstTrackArtist
//                            }
//                        } else {
//                            false
//                        }
//                    )
//                }
//        }

        override suspend fun getOnlyAlbumListFrom(uri: Uri): List<AppListingAlbum> {
            val chosenDir = DocumentFile.fromTreeUri(
                context,
                uri
            )!! // always non-null because returns null only if sdk < 21
            val albumWrapperList = chosenDir.getOnlyInnerStoredAlbums() +
                    AlbumForAlbumListDataWrapper(
                        name = chosenDir.name ?: getDefaultName(),
                        folderPath = uri.toString() // todo maybe just uri everywhere
                    )

            return albumWrapperList
                .map { albumWrapper ->
                    AppListingAlbum(
                        folderPath = albumWrapper.folderPath,
                        name = albumWrapper.name,
                    )
                }
        }

        override suspend fun getAlbumFrom(uri: Uri): AppPlayingAlbum {
            val chosenDir = DocumentFile.fromTreeUri(
                context,
                uri
            )!! // always non-null because returns null only if sdk < 21
            val trackList = chosenDir.getTrackList()
            val artistOfFirstTrack = trackList[0].artist
            return AppPlayingAlbum(
                folderPath = uri.toString(), // todo do i need id at all
                name = chosenDir.name ?: getDefaultName(),
                trackList = trackList,
                albumOfOneArtist = if (artistOfFirstTrack != null) {
                    trackList.all { track ->
                        track.artist == artistOfFirstTrack
                    }
                } else {
                    false
                }
            )
        }

        private fun DocumentFile.getOnlyInnerStoredAlbums(): List<AlbumForAlbumListDataWrapper> {
            return listFiles().mapNotNull { file ->
                if (file.isDirectory) {
                    file.getOnlyInnerStoredAlbums() + // getting inner albums from current iterating directory
                            AlbumForAlbumListDataWrapper(
                                name = file.name ?: getDefaultName(),
                                folderPath = file.uri.toString()
                            ) // creates album from current iterating directory with it's inner tracks
                } else {
                    null
                }
            }.flatten()
        }

        private fun DocumentFile.getInnerStoredAlbums(): List<AlbumForPlayingDataWrapper> {
            return listFiles().mapNotNull { file ->
                if (file.isDirectory) {
                    file.getInnerStoredAlbums() + // getting inner albums from current iterating directory
                            AlbumForPlayingDataWrapper(
                                file.name ?: getDefaultName(),
                                file.getTrackList()
                            ) // creates album from current iterating directory with it's inner tracks
                } else {
                    null
                }
            }.flatten()
        }

        private fun DocumentFile.getTrackList(): List<AppAudioTrack> =
            listFiles().mapNotNull { file ->
                if (file.type != null && isAudioType(file.type!!)) {
                    Log.e("scanning", file.name.toString())
                    val mmr = MediaMetadataRetriever()
                    try {
                        mmr.setDataSource(context, file.uri)
                        val duration =
                            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                ?.toLong() ?: 0 // todo get image as well
                        val albumCover = try {
                            val pic = mmr.embeddedPicture ?: throw Exception()
                            BitmapFactory.decodeByteArray(pic, 0, pic.size)
                        } catch (e: Exception) {
                            null
                        } // todo handle idiomatic way
                        val titleRetrieved =
                            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                        val artistRetrieved =
                            mmr.extractMetadata((MediaMetadataRetriever.METADATA_KEY_ARTIST))
                        AppAudioTrack(
                            filePath = file.uri.toString(),
                            title = titleRetrieved ?: file.name?.removeUnderscoresAndPathSegment()
                            ?: getDefaultName(),
                            artist = artistRetrieved,
//                        albumArtist = albumArtist,
                            duration = duration,
                            albumCover = albumCover
                        )
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }
            }

        private fun getDefaultName(): String =
            context.getString(R.string.unknown_name)

        private fun isAudioType(type: String): Boolean = AUDIO_TYPES_LIST.contains(type)

        companion object {

            val AUDIO_TYPES_LIST = listOf("audio/flac", "audio/mp4", "audio/mp3", "audio/mpeg")
        }

        /**
         * Class that is used to temporary hold album data before mapping it to domain album object
         */
        class AlbumForPlayingDataWrapper(
            val name: String,
            val trackList: List<AppAudioTrack>
        )

        class AlbumForAlbumListDataWrapper(
            val name: String,
            val folderPath: String
        )
    }
}