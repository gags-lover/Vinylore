package com.github.astat1cc.vinylore.core.models.domain

sealed class ErrorType {

    class Generic(val message: String?) : ErrorType()

    object NoAlbumSelected : ErrorType()

    object DirIsEmpty : ErrorType()

    object AlbumIsEmpty : ErrorType()

    object NoTrackList : ErrorType()

    object CancellationError : ErrorType()

    /**
     * For cases when user saved some folder, then deleted it and opens it in the app, or if
     * folder has unreadable type.
     */
    object CanNotReadFolder : ErrorType()
}