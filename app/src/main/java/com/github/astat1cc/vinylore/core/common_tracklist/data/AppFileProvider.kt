package com.github.astat1cc.vinylore.core.common_tracklist.data

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.models.domain.AppAlbum
import com.github.astat1cc.vinylore.core.models.domain.AppAudioTrack
import com.github.astat1cc.vinylore.player.ui.util.removeUnderscoresAndPathSegment

interface AppFileProvider {

    suspend fun getAlbumListFrom(uri: Uri): List<AppAlbum>

    class Impl(
        private val context: Context
    ) : AppFileProvider {

        override suspend fun getAlbumListFrom(uri: Uri): List<AppAlbum> {
            val chosenDir = DocumentFile.fromTreeUri(
                context,
                uri
            )!! // always non-null because returns null only if sdk < 21
            val albumWrapperList = chosenDir.getInnerStoredAlbums() +
                    AlbumDataWrapper(
                        chosenDir.name ?: getDefaultName(), chosenDir.getTrackList()
                    )
            return albumWrapperList
                .filter { albumData -> albumData.trackList.isNotEmpty() }
                .mapIndexed { index, albumWrapper ->
                    AppAlbum(
                        id = index,
                        name = albumWrapper.name,
                        trackList = albumWrapper.trackList
                    )
                }
        }

        private fun DocumentFile.getInnerStoredAlbums(): List<AlbumDataWrapper> {
            return listFiles().mapNotNull { file ->
                if (file.isDirectory) {
                    file.getInnerStoredAlbums() + // getting inner albums from current iterating directory
                            AlbumDataWrapper(
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
                    val mmr = MediaMetadataRetriever()
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
                    AppAudioTrack(
                        file.uri.toString(),
                        file.name?.removeUnderscoresAndPathSegment() ?: getDefaultName(),
                        duration,
                        albumCover
                    )
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
        class AlbumDataWrapper(
            val name: String,
            val trackList: List<AppAudioTrack>
        )
    }
}