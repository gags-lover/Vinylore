package com.github.astat1cc.vinylore.tracklist.ui.models

import com.github.astat1cc.vinylore.core.models.ui.AlbumUi

sealed class UiState {

    class Success(val trackList: List<AlbumUi>?) : UiState()

    class Fail(val message: String) : UiState()

    object Loading : UiState()
}