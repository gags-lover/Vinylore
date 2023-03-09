package com.github.astat1cc.vinylore.player.ui.models

import com.github.astat1cc.vinylore.core.models.ui.PlayingAlbumUi

data class PlayerScreenUiStateData(
    val album: PlayingAlbumUi?,
    val discChosen: Boolean
)