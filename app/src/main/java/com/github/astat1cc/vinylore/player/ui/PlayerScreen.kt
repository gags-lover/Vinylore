package com.github.astat1cc.vinylore.player.ui.vinyl.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.player.ui.AudioViewModel
import com.github.astat1cc.vinylore.player.ui.tonearm.TonearmAnimated
import com.github.astat1cc.vinylore.tracklist.ui.models.UiState
import org.koin.androidx.compose.getViewModel

@Composable
fun PlayerScreen(
    navController: NavHostController,
    albumId: Int,
    viewModel: AudioViewModel = getViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    viewModel.saveCurrentPlayingAlbumId(albumId)

    val uiState = viewModel.uiState.collectAsState()
    val discAnimationState = viewModel.playerAnimationState.collectAsState()
    val tonearmAnimationState = viewModel.tonearmAnimationState.collectAsState()

    DisposableEffect(lifecycleOwner) {
        val visibilityObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.composableIsVisible()
            } else if (event == Lifecycle.Event.ON_STOP) {
                viewModel.composableIsInvisible()
            }
        }
        lifecycleOwner.lifecycle.addObserver(visibilityObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(visibilityObserver)
        }
    }

//    Box(modifier = Modifier.fillMaxSize()) {
//        Image(
//            modifier = Modifier.fillMaxSize(),
//            painter = painterResource(R.drawable.player_background),
//            contentDescription = null,
//            contentScale = ContentScale.FillHeight
//        )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
//                .background(Color.Gray)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_list),
                contentDescription = "", // todo
                tint = MaterialTheme.colors.onPrimary,
                modifier = Modifier
                    .align(CenterEnd)
                    .clip(CircleShape)
                    .clickable(onClick = {

                    })
                    .size(48.dp)
                    .padding(12.dp)
            )
        }
        Row(
//            modifier = Modifier.align(Alignment.Start)
        ) {
            val size = 280.dp
            VinylAnimated(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(size),
                playerState = discAnimationState.value,
                playerStateTransition = { oldState ->
                    viewModel.resumePlayerAnimationStateFrom(oldState)
                }
            )
            TonearmAnimated(
                modifier = Modifier
                    .height(size)
                    .padding(end = 16.dp),
                tonearmState = tonearmAnimationState.value,
                tonearmTransition = { oldState ->
                    viewModel.resumeTonearmAnimationStateFrom(oldState)
                }
            )
        }
        AudioControl(
            modifier = Modifier
                .align(CenterHorizontally)
                .padding(24.dp)
                .size(64.dp),
            playerState = discAnimationState.value
        ) {
            val uiStateLocal = uiState.value
            if (uiStateLocal !is UiState.Success ||
                uiStateLocal.trackList.isNullOrEmpty()
            ) return@AudioControl // todo handle ui state
            viewModel.playPauseToggle()
        }
    }
//    }
}