package com.github.astat1cc.vinylore.player.ui.vinyl.compose

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.github.astat1cc.vinylore.player.ui.vinyl.VinylDiscState

@Composable
fun VinylAnimated(
    modifier: Modifier = Modifier,
    discState: VinylDiscState = VinylDiscState.STOPPED,
    playerStateTransition: (VinylDiscState) -> Unit
) {
    var currentRotation by remember { mutableStateOf(0f) }

    val rotation = remember { Animatable(currentRotation) }

    LaunchedEffect(discState) {
        when (discState) {
            VinylDiscState.STARTED -> {
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
            VinylDiscState.STOPPING -> {
                if (currentRotation > 0f) {
                    rotation.animateTo(
                        targetValue = currentRotation + 120f,
                        animationSpec = tween(
                            durationMillis = 1333,
                            easing = LinearOutSlowInEasing
                        )
                    ) {
                        if (value == targetValue) playerStateTransition(discState)
                        currentRotation = value
                    }
                }
            }
            VinylDiscState.STARTING -> {
                rotation.animateTo(
                    targetValue = currentRotation + 180f,
                    animationSpec = tween(
                        durationMillis = 1000,
                        easing = FastOutLinearInEasing
                    )
                ) {
                    currentRotation = value
                    if (value > targetValue - 15f) playerStateTransition(discState)
                }
            }
            VinylDiscState.STOPPED -> {}
        }
    }
    Vinyl(modifier = modifier, rotationDegrees = rotation.value)
}

// 3000 and 1250