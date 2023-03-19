package com.github.astat1cc.vinylore.player.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.astat1cc.vinylore.core.theme.*
import com.github.astat1cc.vinylore.player.ui.PlayerScreenViewModel
import com.github.astat1cc.vinylore.player.ui.views.vinyl.AudioControl
import com.github.astat1cc.vinylore.player.ui.views.vinyl.VinylDiscState

@Composable
fun TrackInfoAndControlView(
    modifier: Modifier = Modifier,
//    playingTrackName: String,
    trackProgress: Float,
    sliderDraggingFinished: () -> Unit,
    sliderDragging: (Float) -> Unit,
    tonearmRotation: Float,
    discAnimationState: VinylDiscState,
    togglePlayPause: () -> Unit,
    skipToPrevious: () -> Unit,
    skipToNext: () -> Unit,
    title: String,
    artist: String?,
) {
    Column(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        brownForGradient,
                        darkBackground,
//                        brownForGradient,
//                        darkBackground
                    ),
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (artist != null) {
            Column(
                modifier = Modifier.padding(bottom = 8.dp, top = 32.dp, start = 20.dp, end = 20.dp),
//            Modifier.height(112.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // title
                Text(
                    text = title,
                    fontSize = 24.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(),
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )
                // artist
                Text(
                    text = artist,
                    fontSize = 18.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth(),
                    overflow = TextOverflow.Ellipsis,
                    color = vintagePaper
                )
            }
        } else {
            // if artist == null title represents the file's name, so it'd be placed in 2 lined Text
            // view
            Text(
                text = title,
                fontSize = 24.sp,
                minLines = 2,
                maxLines = 2,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, bottom = 8.dp, top = 32.dp)
                    .fillMaxWidth(),
                overflow = TextOverflow.Ellipsis,
                color = Color.White
            )
        }
        // progress slider
        Slider(
            modifier = Modifier.padding(start = 36.dp, end = 36.dp),
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
            modifier = Modifier.weight(1f),
            discState = discAnimationState,
            clickTogglePlayPause = togglePlayPause,
            clickSkipPrevious = skipToPrevious,
            clickSkipNext = skipToNext
        )
    }
}

@Composable
fun dpToSp(dp: Dp) = with(LocalDensity.current) { dp.toSp() }