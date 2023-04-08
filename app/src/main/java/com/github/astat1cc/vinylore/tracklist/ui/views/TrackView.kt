package com.github.astat1cc.vinylore.tracklist.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.models.ui.AudioTrackUi
import java.util.concurrent.TimeUnit

// todo scroll to currently playing track

@Composable
fun TrackView(
    track: AudioTrackUi,
    trackPosition: Int,
    isCurrentlyPlaying: Boolean,
    onTrackViewClicked: () -> Unit,
    useTitleWithoutArtist: Boolean
) {
    MaterialTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .customBackground(isCurrentlyPlaying)
                .clickable {
                    onTrackViewClicked()
                }
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
//            // track position
//            Text(
//                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
//                text = "$trackPosition",
//                maxLines = 1,
//                fontWeight = FontWeight.SemiBold,
//                fontSize = 16.sp
//            )
            // Track name
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp),
                textAlign = TextAlign.Center,
                text = if (useTitleWithoutArtist || track.artist == null) {
                    track.title.uppercase()
                } else {
                    "${track.artist} - ${track.title}".uppercase()
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            // Duration view
            Text(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                text = track.duration.toDurationStyle(),
                maxLines = 1,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Makes light gray background if isCurrentlyPlaying == true, else nothing.
 */
private fun Modifier.customBackground(isCurrentlyPlaying: Boolean): Modifier =
    if (isCurrentlyPlaying) this.background(Color.LightGray) else this

private const val hourInMillis = 3600000L
private fun Long.toDurationStyle(): String =
    if (this > hourInMillis) {
        val hours = TimeUnit.MILLISECONDS.toHours(this)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(this) - hours * 60
        String.format(
            "%02d:%02d:%02d",
            hours,
            minutes,
            TimeUnit.MILLISECONDS.toSeconds(this) - hours * 3600 - minutes * 60
        )
    } else {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(this)
        String.format(
            "%02d:%02d",
            minutes,
            TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.MINUTES.toSeconds(minutes)
        )
    }