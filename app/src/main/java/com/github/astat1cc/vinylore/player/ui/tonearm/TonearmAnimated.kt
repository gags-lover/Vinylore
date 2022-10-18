package com.github.astat1cc.vinylore.player.ui.tonearm

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.player.ui.vinyl.PlayerState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TonearmAnimated(
    modifier: Modifier = Modifier,
    tonearmState: TonearmState = TonearmState.ON_START_POSITION,
    tonearmTransition: (TonearmState) -> Unit
) {
    var currentRotation by remember { mutableStateOf(-10f) }

    val rotation = remember { Animatable(currentRotation) }

    LaunchedEffect(tonearmState) {
        when (tonearmState) {
            TonearmState.MOVING_TO_DISC -> {
                rotation.animateTo(
                    targetValue = 18f,
                    animationSpec = tween(
                        durationMillis = 1350,
                        easing = LinearEasing
                    )
                ) {
                    if (value == targetValue) tonearmTransition(tonearmState)
                    currentRotation = value
                }
            }
            TonearmState.MOVING_FROM_DISC -> {
                rotation.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 1333,
                        easing = LinearEasing
                    )
                ) {
                    if (value == targetValue) tonearmTransition(tonearmState)
                    currentRotation = value
                }
            }
            TonearmState.MOVING_ON_DISC -> {
                rotation.animateTo(
                    targetValue = 45f,
                    animationSpec = tween(
                        durationMillis = 5000,
                        easing = LinearEasing
                    )
                ) {
                    if (value == targetValue) tonearmTransition(tonearmState)
                    currentRotation = value
                }
            }
            TonearmState.ON_START_POSITION -> {}
            TonearmState.STAYING_ON_DISC -> {}
        }
    }
    Image(
        modifier = modifier
            .graphicsLayer(
                transformOrigin = TransformOrigin(
                    pivotFractionX = 0.5f,
                    pivotFractionY = 0.1f,
                ),
                rotationZ = rotation.value,
            ),
        painter = painterResource(R.drawable.tonearm),
        contentScale = ContentScale.FillWidth,
        contentDescription = null
    )
}

