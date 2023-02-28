package com.github.astat1cc.vinylore.player.ui.views

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.astat1cc.vinylore.core.theme.vintagePaper
import com.github.astat1cc.vinylore.player.ui.PlayerScreenViewModel
import com.github.astat1cc.vinylore.player.ui.views.vinyl.AudioControl
import com.github.astat1cc.vinylore.player.ui.views.vinyl.VinylDiscState

@Composable
fun TrackInfoAndControlView(
    modifier: Modifier = Modifier,
    playingTrackName: String,
    trackProgress: Float,
    sliderDraggingFinished: () -> Unit,
    sliderDragging: (Float) -> Unit,
    tonearmRotation: Float,
    discAnimationState: VinylDiscState,
    togglePlayPause: () -> Unit,
    skipToPrevious: () -> Unit,
    skipToNext: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = playingTrackName,
            fontSize = 24.sp,
//            minLines = 2,
//            maxLines = 2,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(start = 20.dp, top = 40.dp, end = 20.dp)
                .fillMaxWidth(),
            overflow = TextOverflow.Ellipsis,
            color = Color.White // todo or vintage paper? and think about everywhere else as well
        )
        // progress slider
        Slider(
            modifier = Modifier.padding(start = 36.dp, end = 36.dp, top = 24.dp),
            value = trackProgress,
            onValueChangeFinished = sliderDraggingFinished,
            onValueChange = sliderDragging,

            //it means user have already launched player, so seeking should be available
            enabled = tonearmRotation >= PlayerScreenViewModel.VINYL_TRACK_START_TONEARM_ROTATION,

            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTickColor = vintagePaper
            )
        )
        AudioControl(
            discState = discAnimationState,
            clickTogglePlayPause = togglePlayPause,
            clickSkipPrevious = skipToPrevious,
            clickSkipNext = skipToNext
        )
    }
}