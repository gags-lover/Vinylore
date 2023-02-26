package com.github.astat1cc.vinylore.player.ui.tonearm

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.player.ui.PlayerScreenViewModel
import kotlinx.coroutines.delay

@Composable
fun TonearmAnimated(
    modifier: Modifier = Modifier,
    currentRotation: Float = 0f,
    tonearmState: TonearmState = TonearmState.ON_START_POSITION,
    tonearmTransition: (TonearmState) -> Unit,
    changeRotation: (Float) -> Unit,
    tonearmLifted: Boolean
) {

    val rotation = remember { Animatable(currentRotation) }

    LaunchedEffect(tonearmState) {
        when (tonearmState) {
            TonearmState.MOVING_TO_START_POSITION -> {
                delay(1500L)
                rotation.animateTo(
                    targetValue = PlayerScreenViewModel.VINYL_TRACK_START_TONEARM_ROTATION,
                    animationSpec = tween(
                        durationMillis = 1350,
                        easing = LinearEasing
                    )
                ) {
                    changeRotation(value)
                    if (value == targetValue) tonearmTransition(tonearmState)
                }
            }
            TonearmState.MOVING_TO_IDLE_POSITION -> {
                rotation.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 1333,
                        easing = LinearEasing
                    )
                ) {
                    changeRotation(value)
                    if (value == targetValue) tonearmTransition(tonearmState)
                }
            }
            TonearmState.MOVING_ABOVE_DISC -> {
//                while (true) {
//                    val newRotation = VINYL_TRACK_START_TONEARM_ROTATION + audioProgress / 2.85f
                rotation.snapTo(currentRotation)
//                    delay(500L)
//                }
            }
            TonearmState.ON_START_POSITION -> {
//                if (currentRotation == 0f)
//                delay(1000L)
                rotation.snapTo(currentRotation)
            }
            TonearmState.STAYING_ON_DISC -> {
                rotation.snapTo(currentRotation)
            }
        }
//        Log.e("state", "in ton anim: $tonearmState, $currentRotation, ${rotation.value}")
    }
    Box {
        // additional shadow
        AnimatedVisibility(
            visible = tonearmLifted,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Image(
                modifier = modifier
                    .graphicsLayer(
                        transformOrigin = TransformOrigin(
                            pivotFractionX = 0.59f,
                            pivotFractionY = 0.226f,
                        ),
                        rotationZ = currentRotation,
                    ),
                painter = painterResource(R.drawable.additional_tonearm_shadow),
                contentScale = ContentScale.Fit,
                contentDescription = null
            )
        }
        // tonearm
        Image(
            modifier = modifier
                .graphicsLayer(
                    transformOrigin = TransformOrigin(
                        pivotFractionX = 0.59f,
                        pivotFractionY = 0.226f,
                    ),
                    rotationZ = currentRotation,
                ),
            painter = painterResource(R.drawable.tonearm),
            contentScale = ContentScale.Fit,
            contentDescription = null
        )
    }
}

