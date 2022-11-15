package com.github.astat1cc.vinylore.tracklist.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.models.ui.AudioTrackUi

@Composable
fun TrackView(track: AudioTrackUi) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .align(Alignment.Center),
            textAlign = TextAlign.Center,
            text = track.name.toTrackNameStyle() ?: stringResource(id = R.string.unknown_name),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

private fun String?.toTrackNameStyle(): String? =
    if (this == null) null else this.replace("_", " ")
        .replaceAfterLast(".", "")
        .dropLast(1)
        .uppercase()