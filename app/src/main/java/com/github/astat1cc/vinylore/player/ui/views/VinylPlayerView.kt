package com.github.astat1cc.vinylore.player.ui.views

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.AppConst
import com.github.astat1cc.vinylore.core.models.ui.AudioTrackUi
import com.github.astat1cc.vinylore.core.theme.brownForGradient
import com.github.astat1cc.vinylore.core.theme.darkBackground
import com.github.astat1cc.vinylore.core.theme.steelGray
import com.github.astat1cc.vinylore.player.ui.views.tonearm.TonearmAnimated
import com.github.astat1cc.vinylore.player.ui.views.tonearm.TonearmState
import com.github.astat1cc.vinylore.player.ui.views.vinyl.VinylAnimated
import com.github.astat1cc.vinylore.player.ui.views.vinyl.VinylDiscState
import kotlinx.coroutines.delay

@Composable
fun VinylPlayerView(
    modifier: Modifier = Modifier,
    vinylSize: Dp,
    discAnimationState: VinylDiscState,
    playerStateTransition: (VinylDiscState) -> Unit,
    discRotation: Float,
    changeVinylRotation: (Float) -> Unit,
    playingTrack: AudioTrackUi?,
    tonearmRotation: Float,
    tonearmAnimationState: TonearmState,
    tonearmLifted: Boolean,
    tonearmTransition: (TonearmState) -> Unit,
    changeTonearmRotation: (Float) -> Unit,
    shouldShowVinylAppearanceAnimation: Boolean?,
    vinylAppearanceAnimationShown: () -> Unit,
    orientationPortrait: Boolean
) {
    // Delay to improve animation experience. Inverting boolean param because AnimatedVisibility
    // shows animation any time this variable is changed, so if we shouldn't show animation
    // (if param is false or null) we need to emit this variable to true initially, so animation wouldn't
    // appear.
//    var vinylIsVisible by remember {
//        mutableStateOf(
//            shouldShowVinylAppearanceAnimation != null && !shouldShowVinylAppearanceAnimation
//            false
//        )
//    }
//    Log.e(
//        "appearance",
//        "shouldShowAppearance $shouldShowVinylAppearanceAnimation, vinyl is Visible $vinylIsVisible"
//    )

    LaunchedEffect(shouldShowVinylAppearanceAnimation) {
        if (shouldShowVinylAppearanceAnimation == true) {
            delay(AppConst.SLIDE_IN_DURATION.toLong())
            vinylAppearanceAnimationShown()
        }
    }
//    LaunchedEffect(shouldShowVinylAppearanceAnimation) {
//        if (shouldShowVinylAppearanceAnimation == true) {
//            Log.e("appear", "before delay")
//            delay(1000L)
//            vinylIsVisible = true
//            Log.e("appear", "before shown call")
//            vinylAppearanceAnimationShown()
//        } else {
//            Log.e("appear", "before emitting false call")
//            vinylIsVisible = false
//        }
//    }

    val modifierConsideringOrientation =
        if (orientationPortrait) modifier.fillMaxWidth() else modifier
//    Box(){
//        // this it to hide black background in the bottom when corners are rounded
//        Box(
//            modifier = Modifier
//                .align(Alignment.BottomStart)
//                .fillMaxWidth()
//                .height(8.dp)
//                .background(brownForGradient)
//        )
    val patchModifierConsideringOrientation =
        if (orientationPortrait) Modifier.fillMaxWidth() else Modifier
    Box(
        modifier = modifierConsideringOrientation
//            .background(brownForGradient)
        ,
        contentAlignment = Alignment.Center
    ) {
        if (orientationPortrait) {
            Box(
                patchModifierConsideringOrientation
                    .height(8.dp)
                    .align(Alignment.TopStart)
                    .background(darkBackground)
            )
            Box(
                patchModifierConsideringOrientation
                    .height(20.dp)
                    .align(Alignment.BottomStart)
                    .background(brownForGradient)
            )
        } else {
            Box(
                // it gives 8.dp height line on top with fillMaxWidth behaviour. Can't use fillMaxWidth
                // itself because then player view fills all the screen, while it shouldn't as long
                // as orientation is landscape
                patchModifierConsideringOrientation
                    .padding(bottom = vinylSize - 8.dp)
                    .matchParentSize()
                    .align(Alignment.TopStart)
                    .background(brownForGradient)
            )
        }
        // background
        Image(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .shadow(elevation = 6.dp, clip = true)
                .matchParentSize()
                .clip(RoundedCornerShape(8.dp)),
            painter = painterResource(id = R.drawable.wood_background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds
        )

        Box(
            modifier = Modifier.padding(
                top = 32.dp, bottom = 40.dp,
                start = 16.dp, end = 16.dp
            )
        ) {
            // Vinyl platter
            Box(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(vinylSize)
                    .padding(vinylSize * 0.05f)
                    .clip(CircleShape)
                    .border(width = 4.dp, color = steelGray, shape = CircleShape)
                    .background(Color.Black)
                    .align(Alignment.CenterStart),
            )
            AnimatedVisibility(
                visible = playingTrack != null && shouldShowVinylAppearanceAnimation == true,
                enter = slideInVertically(
                    initialOffsetY = { -it * 3 / 2 },
                    animationSpec = tween(
                        easing = LinearOutSlowInEasing,
                        durationMillis = AppConst.SLIDE_IN_DURATION
                    )
                ),
                exit = shrinkOut(
//                    targetOffsetX = { -it * 3 / 2 },
                    animationSpec = tween(
                        easing = FastOutLinearInEasing,
                        durationMillis = AppConst.VINYL_VISIBILITY_SHRINK_OUT_DURATION
                    ),
                    clip = false
                )
            ) {
                VinylAnimated(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .width(vinylSize)
                        .align(Alignment.CenterStart)
//                        .alpha(0.5f)
                    ,
                    discState = discAnimationState,
                    playerStateTransitionFrom = playerStateTransition,
                    currentRotation = discRotation,
                    changeRotation = changeVinylRotation,
                    albumCover = playingTrack?.albumCover
                )
            }
            TonearmAnimated(
                modifier = Modifier.align(Alignment.CenterStart)
//                    .padding(start = vinylSize) // todo maybe row
//                    .height(vinylSize)
//                    .align(Alignment.CenterStart)
                ,
                vinylSize = vinylSize,
                currentRotation = tonearmRotation,
                tonearmState = tonearmAnimationState,
                tonearmTransition = tonearmTransition,
                changeRotation = changeTonearmRotation,
                tonearmLifted = tonearmLifted
            )
        }
//            Image(
//                modifier = Modifier.matchParentSize(),
//                painter = painterResource(id = R.drawable.glass),
//                contentDescription = null,
//                contentScale = ContentScale.FillBounds,
//                alpha = 0.4f
//            )
    }
}