package com.github.astat1cc.vinylore.player.ui.models

import com.github.astat1cc.vinylore.core.models.ui.AudioTrackUi

data class PlayerScreenUiStateData(
    val trackList: List<AudioTrackUi>,
    val discChosen: Boolean
)