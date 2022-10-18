package com.github.astat1cc.vinylore.player.ui.vinyl.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.astat1cc.vinylore.player.ui.vinyl.PlayerState

@Composable
fun VinylAnimated(
    modifier: Modifier = Modifier,
    playerState: PlayerState = PlayerState.STOPPED,
    playerStateTransition: (PlayerState) -> Unit
) {
    var currentRotation by remember { mutableStateOf(0f) }

    val rotation = remember { Animatable(currentRotation) }

    LaunchedEffect(playerState) {
        when (playerState) {
            PlayerState.STARTED -> {
                rotation.animateTo(
                    targetValue = currentRotation + 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1333, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                ) {
                    currentRotation = value
                }
            }
            PlayerState.STOPPING -> {
                if (currentRotation > 0f) {
                    rotation.animateTo(
                        targetValue = currentRotation + 120f,
                        animationSpec = tween(
                            durationMillis = 1333,
                            easing = LinearOutSlowInEasing
                        )
                    ) {
                        if (value == targetValue) playerStateTransition(playerState)
                        currentRotation = value
                    }
                }
            }
            PlayerState.STARTING -> {
                rotation.animateTo(
                    targetValue = currentRotation + 180f,
                    animationSpec = tween(
                        durationMillis = 1000,
                        easing = FastOutLinearInEasing
                    )
                ) {
                    currentRotation = value
                    if (value > targetValue - 15f) playerStateTransition(playerState)
                }
            }
            PlayerState.STOPPED -> {}
        }
    }
    Vinyl(modifier = modifier, rotationDegrees = rotation.value)
}

// 3000 and 1250