package com.github.astat1cc.vinylore.player.ui.views.vinyl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.theme.brown
import com.github.astat1cc.vinylore.core.theme.darkBackground

@Composable
fun AudioControl(
    modifier: Modifier = Modifier,
    discState: VinylDiscState = VinylDiscState.STOPPED,
    tint: Color = MaterialTheme.colors.onPrimary,
    clickTogglePlayPause: () -> Unit,
    clickSkipNext: () -> Unit,
    clickSkipPrevious: () -> Unit,
) {
    val secondaryButtonStandardSize = 56.dp
    val secondaryButtonStandardModifier = Modifier
        .padding(horizontal = 8.dp)
        .size(secondaryButtonStandardSize)
        .clip(CircleShape)
    Row(
        modifier = modifier.padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_skip_previous),
            contentDescription = null, // todo content description
            tint = Color.White,
            modifier = secondaryButtonStandardModifier
                .clickable(onClick = { clickSkipPrevious() })
                .padding(8.dp)
        )
        Icon(
            painter = painterResource(
                id = when (discState) {
                    VinylDiscState.STARTING, VinylDiscState.STARTED -> R.drawable.ic_pause
                    else -> R.drawable.ic_play
                }
            ),
            contentDescription = null, // todo content description
            tint = darkBackground,
            modifier = Modifier
                .padding(8.dp)
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = { clickTogglePlayPause() })
                .padding(12.dp)
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_skip_next),
            contentDescription = null, // todo content description
            tint = Color.White,
            modifier = secondaryButtonStandardModifier
                .clickable(onClick = { clickSkipNext() })
                .padding(8.dp)
        )
    }
}