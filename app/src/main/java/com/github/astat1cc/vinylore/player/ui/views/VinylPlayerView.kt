package com.github.astat1cc.vinylore.player.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.astat1cc.vinylore.core.models.ui.AudioTrackUi
import com.github.astat1cc.vinylore.player.ui.views.tonearm.TonearmAnimated
import com.github.astat1cc.vinylore.player.ui.views.tonearm.TonearmState
import com.github.astat1cc.vinylore.player.ui.views.vinyl.VinylAnimated
import com.github.astat1cc.vinylore.player.ui.views.vinyl.VinylDiscState

@Composable
fun VinylPlayerView(
    modifier: Modifier = Modifier,
    vinylSize: Dp,
    discAnimationState: VinylDiscState,
    playerStateTransition: (VinylDiscState) -> Unit,
    discRotation: Float,
    changeVinylRotation: (Float) -> Unit,
    playingTrack: AudioTrackUi?,
    tonearmRotation: Float,
    tonearmAnimationState: TonearmState,
    tonearmLifted: Boolean,
    tonearmTransition: (TonearmState) -> Unit,
    changeTonearmRotation: (Float) -> Unit
) {
    Box(
        modifier = modifier
            .padding(top = 32.dp)
    ) {
        VinylAnimated(
            modifier = Modifier
                .padding(start = 16.dp)
                .width(vinylSize)
                .align(Alignment.CenterStart),
            discState = discAnimationState,
            playerStateTransitionFrom = playerStateTransition,
            currentRotation = discRotation,
            changeRotation = changeVinylRotation,
            albumCover = playingTrack?.albumCover
        )
        TonearmAnimated(
            modifier = Modifier
                .padding(start = vinylSize) // todo maybe row
                .height(vinylSize)
                .align(Alignment.CenterStart),
            currentRotation = tonearmRotation,
            tonearmState = tonearmAnimationState,
            tonearmTransition = tonearmTransition,
            changeRotation = changeTonearmRotation,
            tonearmLifted = tonearmLifted
        )
    }
}