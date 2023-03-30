package com.github.astat1cc.vinylore.player.ui.views.tonearm

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.AppConst
import kotlinx.coroutines.delay

@Composable
fun TonearmAnimated(
    modifier: Modifier = Modifier,
    currentRotation: Float = 0f,
    tonearmState: TonearmState = TonearmState.IDLE_POSITION,
    tonearmTransition: (TonearmState) -> Unit,
    changeRotation: (Float) -> Unit,
    tonearmLifted: Boolean,
    vinylSize: Dp
) {

    // using normal rotation * 100 because it has measurement error
    val rotationAnimatable = remember { Animatable(currentRotation) }

    LaunchedEffect(tonearmState) {
        rotationAnimatable.snapTo(currentRotation)
        when (tonearmState) {
            TonearmState.MOVING_FROM_IDLE_TO_START_POSITION -> {
                delay(1500L)
                rotationAnimatable.animateTo(
                    targetValue = AppConst.VINYL_TRACK_START_TONEARM_ROTATION,
                    animationSpec = tween(
                        durationMillis = 1350,
                        easing = LinearEasing
                    )
                ) {
                    changeRotation(value)
                    if (value == targetValue) tonearmTransition(tonearmState)
                }
            }
            TonearmState.MOVING_FROM_DISC_TO_IDLE_POSITION -> {
                rotationAnimatable.animateTo(
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
                rotationAnimatable.snapTo(currentRotation)
            }
            TonearmState.IDLE_POSITION -> {
                rotationAnimatable.snapTo(currentRotation)
            }
            TonearmState.STAYING_ON_DISC -> {
                rotationAnimatable.snapTo(currentRotation)
            }
            TonearmState.MOVING_FROM_DISC_TO_START_POSITION -> {
                rotationAnimatable.animateTo(
                    targetValue = AppConst.VINYL_TRACK_START_TONEARM_ROTATION,
                    animationSpec = tween(
                        durationMillis = AppConst.TONEARM_MOVING_FROM_DISC_TO_START_POSITION_DURATION,
                        easing = LinearEasing
                    )
                ) {
                    changeRotation(value)
                }
            }
        }
    }
    val tonearmAxisTransformOrigin =
        TransformOrigin(pivotFractionX = 0.59f, pivotFractionY = 0.226f)
    Box(
        modifier = modifier
            .padding(start = vinylSize)
            .height(vinylSize)
    ) {
        Image(
            modifier = Modifier
                .size(vinylSize * 0.2f)
                .offset(y = vinylSize * 0.128f, x = vinylSize * 0.114f),
            painter = painterResource(R.drawable.tonearm_platter),
            contentDescription = null
        )
        // additional shadow
        if (tonearmLifted) {
            Image(
                modifier = Modifier
                    .fillMaxHeight()
                    .graphicsLayer(
                        transformOrigin = tonearmAxisTransformOrigin,
                        rotationZ = currentRotation,
                    ),
                painter = painterResource(R.drawable.additional_tonearm_shadow),
                contentScale = ContentScale.Fit,
                contentDescription = null
            )
        }
        // tonearm
        Image(
            modifier = Modifier
                .fillMaxHeight()
                .graphicsLayer(
                    transformOrigin = tonearmAxisTransformOrigin,
                    rotationZ = currentRotation,
                ),
            painter = painterResource(R.drawable.tonearm),
            contentScale = ContentScale.Fit,
            contentDescription = null
        )
    }
}

