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
import com.github.astat1cc.vinylore.player.ui.util.removeUnderscoresAndPathSegment
import java.util.concurrent.TimeUnit

// todo scroll to currently playing track

@Composable
fun TrackView(
    track: AudioTrackUi,
    isCurrentlyPlaying: Boolean,
    onTrackViewClicked: () -> Unit
) {
    MaterialTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .customBackground(isCurrentlyPlaying)
                .clickable {
                    onTrackViewClicked()
                }
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track name
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp),
                textAlign = TextAlign.Center,
                text = track.name?.uppercase() ?: stringResource(id = R.string.unknown_name),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            // Duration view
            Text(
                modifier = Modifier.padding(12.dp),
                text = track.duration.toDurationStyle(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

/**
 * Makes light gray background if isCurrentlyPlaying == true, else nothing.
 */
private fun Modifier.customBackground(isCurrentlyPlaying: Boolean): Modifier =
    if (isCurrentlyPlaying) this.background(Color.LightGray) else this

private val hourInMillis = 3600000L
private fun Long.toDurationStyle(): String =
    if (this > hourInMillis) {
        val hours = TimeUnit.MILLISECONDS.toHours(this)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(this) - TimeUnit.MINUTES.toMinutes(hours)
        String.format(
            "%02d:%02d:%02d",
            hours,
            minutes,
            TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.MINUTES.toSeconds(minutes)
        )
    } else {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(this)
        String.format(
            "%02d:%02d",
            minutes,
            TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.MINUTES.toSeconds(minutes)
        )
    }