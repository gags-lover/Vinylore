package com.github.astat1cc.vinylore.tracklist.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.models.domain.AppAlbum
import com.github.astat1cc.vinylore.core.models.domain.AppAudioTrack

interface AppFileProvider {

    suspend fun getAlbumListFrom(uri: Uri): List<AppAlbum>

    class Impl(
        private val context: Context,
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
                    AppAlbum(index, albumWrapper.name, albumWrapper.trackList)
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
                    AppAudioTrack(file.uri.toString(), file.name ?: getDefaultName())
                } else {
                    null
                }
            }

        private fun getDefaultName(): String =
            context.getString(R.string.unknown_name)

        private fun isAudioType(type: String): Boolean = type.startsWith(AUDIO_TYPE_PREFIX)

        companion object {

            const val AUDIO_TYPE_PREFIX = "audio/"
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