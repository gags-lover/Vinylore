package com.github.astat1cc.vinylore.player.ui.vinyl.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.player.ui.vinyl.VinylDiscState

@Composable
fun AudioControl(
    modifier: Modifier = Modifier,
    discState: VinylDiscState = VinylDiscState.STOPPED,
    tint: Color = MaterialTheme.colors.onPrimary,
    clickTogglePlayPause: () -> Unit
) {
    Icon(
        painter = painterResource(
            id = when (discState) {
                VinylDiscState.STARTING, VinylDiscState.STARTED -> R.drawable.ic_pause
                else -> R.drawable.ic_play
            }
        ),
        contentDescription = null, // todo content description
        tint = tint,
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = { clickTogglePlayPause() })
            .padding(8.dp)
    )
}