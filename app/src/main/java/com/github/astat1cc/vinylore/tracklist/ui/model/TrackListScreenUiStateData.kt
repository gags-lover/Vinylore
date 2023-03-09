package com.github.astat1cc.vinylore.tracklist.ui.model

import com.github.astat1cc.vinylore.core.models.ui.PlayingAlbumUi
import com.github.astat1cc.vinylore.core.models.ui.AudioTrackUi

data class TrackListScreenUiStateData(
    val album: PlayingAlbumUi?,
    val currentPlayingTrack: AudioTrackUi?
)