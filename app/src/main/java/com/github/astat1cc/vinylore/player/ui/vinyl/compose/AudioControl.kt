package com.github.astat1cc.vinylore.player.ui.vinyl.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.github.astat1cc.vinylore.player.ui.vinyl.PlayerState

@Composable
fun AudioControl(
    modifier: Modifier = Modifier,
    playerState: PlayerState = PlayerState.STOPPED,
    tint: Color = MaterialTheme.colors.onPrimary,
    clickTogglePlayPause: ((PlayerState) -> Unit)
) {
    Icon(
        imageVector = when (playerState) {
            PlayerState.STARTED -> Icons.Default.Done
            else -> Icons.Filled.PlayArrow // todo make pause icon
        },
        contentDescription = "", // todo content description
        tint = tint,
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = {
                clickTogglePlayPause(playerState)
            })
    )
}