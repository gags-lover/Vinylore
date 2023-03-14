package com.github.astat1cc.vinylore.player.ui.views

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
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
import androidx.compose.ui.unit.dp
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.models.ui.AudioTrackUi
import com.github.astat1cc.vinylore.core.theme.brownForGradient
import com.github.astat1cc.vinylore.core.theme.darkBackground
import com.github.astat1cc.vinylore.core.theme.newBrown
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
    var vinylIsVisible by remember {
        mutableStateOf(shouldShowVinylAppearanceAnimation != null && !shouldShowVinylAppearanceAnimation)
    }

    LaunchedEffect(shouldShowVinylAppearanceAnimation) {
        if (shouldShowVinylAppearanceAnimation != true) return@LaunchedEffect
        delay(400L)
        vinylIsVisible = true
        vinylAppearanceAnimationShown()
    }

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
    Box(
        modifier = modifierConsideringOrientation
//            .background(brownForGradient)
        ,
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .align(Alignment.TopStart)
                .background(darkBackground)
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(20.dp)
                .align(Alignment.BottomStart)
                .background(brownForGradient)
        )
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

        Box(modifier = Modifier.padding(top = 32.dp, bottom = 40.dp)) {
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
                visible = playingTrack != null && vinylIsVisible,
                enter = slideInHorizontally(initialOffsetX = { it })
            ) {
                VinylAnimated(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .width(vinylSize)
                        .align(Alignment.CenterStart),
                    discState = discAnimationState,
                    playerStateTransitionFrom = playerStateTransition,
                    currentRotation = discRotation,
                    changeRotation = changeVinylRotation,
                    albumCover = playingTrack?.albumCover
                )
            }
            TonearmAnimated(
                modifier = Modifier
                    .padding(start = vinylSize) // todo maybe row
                    .height(vinylSize)
                    .align(Alignment.CenterStart),
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