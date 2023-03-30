package com.github.astat1cc.vinylore.player.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.astat1cc.vinylore.core.AppConst
import com.github.astat1cc.vinylore.core.models.ui.AudioTrackUi
import com.github.astat1cc.vinylore.core.theme.*
import com.github.astat1cc.vinylore.player.ui.views.vinyl.AudioControl

@Composable
fun TrackInfoAndControlView(
    modifier: Modifier = Modifier,
//    playingTrackName: String,
    trackProgress: Float,
    sliderDraggingFinished: () -> Unit,
    sliderDragging: (Float) -> Unit,
    tonearmRotation: Float,
    togglePlayPause: () -> Unit,
    skipToPrevious: () -> Unit,
    skipToNext: () -> Unit,
    playingTrack: AudioTrackUi?,
    isPlaying: Boolean,
    playPauseToggleBlocked: Boolean,
    sliderEnabled: Boolean
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
        // track name
        if (playingTrack?.artist != null) {
            Column(
                modifier = Modifier.padding(top = 32.dp, start = 20.dp, end = 20.dp),
//            Modifier.height(112.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // title
                Text(
                    text = playingTrack.title,
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
                    text = playingTrack.artist,
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
                text = playingTrack?.title
                    ?: "", // equals "" only when preparing, so no track name is displayed
                fontSize = 24.sp,
                minLines = 2,
                maxLines = 2,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 32.dp)
                    .fillMaxWidth(),
                overflow = TextOverflow.Ellipsis,
                color = Color.White
            )
        }

        // genre, year, bitrate
//        if (track?.genre != null || track?.year != null || track?.bitrate != null) {
        val secondaryInfoItems = playingTrack?.let { track ->
            listOfNotNull(
                track.mediaFormat,
                track.sampleRate?.let { sampleRate -> "$sampleRate kHz" },
                track.bitrate?.let { bitrate -> "$bitrate kbps" })
        }
        Row(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = secondaryInfoItems?.joinToString(
                    separator = " | ",
                    prefix = "",
                    postfix = ""
                ) ?: "",
                fontSize = 14.sp,
                minLines = 1,
                maxLines = 2,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth(),
                overflow = TextOverflow.Ellipsis,
                color = Color.LightGray
            )
        }
//        }

        // progress slider
        Slider(
            modifier = Modifier.padding(start = 36.dp, end = 36.dp),
            value = trackProgress,
            onValueChangeFinished = sliderDraggingFinished,
            onValueChange = sliderDragging,

            //it means user have already launched player, so seeking should be available
            enabled = sliderEnabled,
//            tonearmRotation >= AppConst.VINYL_TRACK_START_TONEARM_ROTATION,

            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
            )
        )
        AudioControl(
            modifier = Modifier.weight(1f),
            isPlaying = isPlaying,
            clickTogglePlayPause = togglePlayPause,
            clickSkipPrevious = skipToPrevious,
            clickSkipNext = skipToNext,
            playPauseToggleBlocked = playPauseToggleBlocked
        )
    }
}

@Composable
fun dpToSp(dp: Dp) = with(LocalDensity.current) { dp.toSp() }