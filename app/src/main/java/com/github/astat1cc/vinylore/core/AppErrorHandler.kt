package com.github.astat1cc.vinylore.core

import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.models.domain.ErrorType
import com.github.astat1cc.vinylore.core.models.exceptions.AlbumIsEmptyException
import com.github.astat1cc.vinylore.core.models.exceptions.CanNotReadFolderException
import com.github.astat1cc.vinylore.core.models.exceptions.NoAlbumSelectedException
import java.lang.IndexOutOfBoundsException
import java.lang.NullPointerException
import java.util.concurrent.CancellationException

interface AppErrorHandler {

    fun getErrorMessage(error: ErrorType): String

    fun getErrorTypeOf(exception: Exception): ErrorType

    class Impl(
        private val resources: AppResourceProvider
    ) : AppErrorHandler {

        override fun getErrorMessage(error: ErrorType): String =
            when (error) {
                is ErrorType.Generic -> error.message
                    ?: resources.getString(R.string.something_went_wrong)
                is ErrorType.NoAlbumSelected -> resources.getString(R.string.no_album_selected)
                is ErrorType.DirIsEmpty -> resources.getString(R.string.dir_is_empty)
                is ErrorType.AlbumIsEmpty -> resources.getString(R.string.album_is_empty)
                is ErrorType.CanNotReadFolder -> resources.getString(R.string.cannot_read_folder)
                is ErrorType.NoTrackList -> resources.getString(R.string.no_track_list)
                is ErrorType.CancellationError -> CancellationErrorMessage
            }

        override fun getErrorTypeOf(exception: Exception): ErrorType =
            when (exception) {
                is CanNotReadFolderException -> ErrorType.CanNotReadFolder
                is AlbumIsEmptyException -> ErrorType.AlbumIsEmpty
                is NoAlbumSelectedException -> ErrorType.NoAlbumSelected

                // it means system killed app and it tried to make something in cancelled coroutine
                // scope
                is CancellationException -> ErrorType.CancellationError

                else -> ErrorType.Generic(exception.message)
            }

        companion object {

            const val CancellationErrorMessage = "CancellationErrorMessage"
        }
    }
}