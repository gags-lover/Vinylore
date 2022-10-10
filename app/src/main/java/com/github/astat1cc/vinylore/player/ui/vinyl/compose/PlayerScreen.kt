package com.github.astat1cc.vinylore.player.ui.vinyl.compose

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.github.astat1cc.vinylore.player.ui.AudioViewModel
import com.github.astat1cc.vinylore.player.ui.vinyl.PlayerState
import org.koin.androidx.compose.getViewModel

@Composable
fun PlayerScreen(
    navController: NavHostController,
    albumId: Int,
    viewModel: AudioViewModel = getViewModel()
) {
    val configuration = LocalConfiguration.current
    val trackList = viewModel.trackList.collectAsState()
    var playingState by remember { mutableStateOf(PlayerState.STOPPED) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.DarkGray,
                        Color.LightGray
                    )
                )
            )
    ) {
        VinylAnimation(
            modifier = Modifier.align(TopCenter),
            playerState = playingState,
            playerStartedListener = {
                playingState = PlayerState.STARTED
            },
            playerStoppedListener = {
                playingState = PlayerState.STOPPED
            }
        )
        AudioControl(
            modifier = Modifier
                .padding(72.dp)
                .size(72.dp)
                .align(
                    when (configuration.orientation) {
                        Configuration.ORIENTATION_LANDSCAPE -> CenterEnd
                        else -> BottomCenter
                    }
                ),
            playerState = playingState
        ) { oldState ->
            val track = trackList.value.first()
            viewModel.playAudio(track)
            playingState = when (oldState) {
                PlayerState.STOPPED, PlayerState.STOPPING -> PlayerState.STARTING
                PlayerState.STARTED, PlayerState.STARTING -> PlayerState.STOPPING
            }
        }
    }
}