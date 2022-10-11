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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

//    val configuration = LocalConfiguration.current
    val uiState = viewModel.uiState.collectAsState()
    val playingAnimationState = viewModel.playerAnimationState.collectAsState()

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
        VinylAnimation(
//            modifier = Modifier.align(TopCenter),
            playerState = playingAnimationState.value,
            playerStateChangedListener = { oldState ->
                viewModel.resumePlayerAnimationStateFrom(oldState)
            }
//            playerStartedListener = {
//                playingAnimationState = PlayerState.STARTED
//            },
//            playerStoppedListener = {
//                playingAnimationState = PlayerState.STOPPED
//            }
        )
        AudioControl(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(24.dp)
                .size(64.dp),
            playerState = playingAnimationState.value
        ) {
            val uiStateLocal = uiState.value
            if (uiStateLocal !is UiState.Success ||
                uiStateLocal.trackList.isNullOrEmpty()
            ) return@AudioControl
            viewModel.playPauseToggle()
        }
    }
}