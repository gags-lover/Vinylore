package com.github.astat1cc.vinylore.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.models.ui.UiState
import com.github.astat1cc.vinylore.core.theme.brown
import com.github.astat1cc.vinylore.navigation.NavigationTree
import com.github.astat1cc.vinylore.player.ui.tonearm.TonearmAnimated
import com.github.astat1cc.vinylore.player.ui.vinyl.compose.AudioControl
import com.github.astat1cc.vinylore.player.ui.vinyl.compose.VinylAnimated
import org.koin.androidx.compose.getViewModel

// todo handle sorting track list
// todo handle bluetooth headphones delay
// todo extra spin when pause pressed
// todo make tonearm move slowly to start position while going to next track

@Composable
fun PlayerScreen(
    navController: NavHostController,
    viewModel: PlayerScreenViewModel = getViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val uiState = viewModel.uiState.collectAsState()
    val albumChoosingCalled = viewModel.albumChoosingCalled.collectAsState()
    val discAnimationState = viewModel.playerAnimationState.collectAsState()
    val tonearmAnimationState = viewModel.tonearmAnimationState.collectAsState()
    val discRotation = viewModel.discRotation.collectAsState()
    val tonearmRotation = viewModel.tonearmRotation.collectAsState()

    val configuration = LocalConfiguration.current
    val vinylSize =
        (configuration.screenWidthDp.dp - 32.dp) * 0.7f  // 32 dp is padding 16 + 16 left and right

    val localState = uiState.value
    // open album choosing screen if currently no album is chosen (happens only once, then user should
    // click album choosing button manually
    if (!albumChoosingCalled.value &&
        localState is UiState.Success &&
        !localState.data.discChosen
    ) {
        viewModel.albumChoosingCalled()
        navController.navigate(NavigationTree.AlbumList.name)
    }

    // sending information every time screen is visible or not to decide to show animation or not
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
            .background(brown)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_folder),
                contentDescription = null,
                tint = MaterialTheme.colors.onPrimary,
                modifier = Modifier
                    .padding(start = 4.dp, top = 4.dp)
                    .align(CenterStart)
                    .clip(CircleShape)
                    .clickable(onClick = {
                        navController.navigate(NavigationTree.AlbumList.name)
                    })
                    .size(48.dp)
                    .padding(12.dp)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_list),
                contentDescription = null, // todo
                tint = MaterialTheme.colors.onPrimary,
                modifier = Modifier
                    .padding(end = 4.dp, top = 4.dp)
                    .align(CenterEnd)
                    .clip(CircleShape)
                    .clickable(onClick = {
                        navController.navigate(NavigationTree.TrackList.name) {
                            navController.popBackStack(
                                NavigationTree.TrackList.name,
                                inclusive = true
                            )
                        }
                    })
                    .size(48.dp)
                    .padding(12.dp)
            )
        }
        Box(modifier = Modifier.align(CenterHorizontally)) {
            VinylAnimated(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .width(vinylSize)
                    .align(CenterStart),
                discState = discAnimationState.value,
                playerStateTransitionFrom = { oldState ->
                    viewModel.resumePlayerAnimationStateFrom(oldState)
                },
                currentRotation = discRotation.value,
                changeRotation = { newRotation ->
                    viewModel.changeDiscRotationFromAnimation(newRotation)
                }
            )
            TonearmAnimated(
                modifier = Modifier
//                    .height(size)
                    .padding(start = vinylSize)
                    .height(vinylSize)
                    .align(CenterStart),
                currentRotation = tonearmRotation.value,
                tonearmState = tonearmAnimationState.value,
                tonearmTransition = { oldState ->
                    viewModel.resumeTonearmAnimationStateFrom(oldState)
                },
                changeRotation = { newRotation ->
                    viewModel.changeTonearmRotationFromAnimation(newRotation)

                }
            )
        }
//        Row {
//            VinylAnimated(
//                modifier = Modifier
//                    .padding(start = 16.dp)
//                    .width(vinylSize),
//                discState = discAnimationState.value,
//                playerStateTransitionFrom = { oldState ->
//                    viewModel.resumePlayerAnimationStateFrom(oldState)
//                },
//                currentRotation = rotation.value,
//                changeRotation = { newRotation ->
//                    viewModel.changeDiscRotationFromAnimation(newRotation)
//                }
//            )
//            TonearmAnimated(
//                modifier = Modifier
////                    .height(size)
//                    .padding(end = 16.dp)
//                    .height(vinylSize),
//                tonearmState = tonearmAnimationState.value,
//                tonearmTransition = { oldState ->
//                    viewModel.resumeTonearmAnimationStateFrom(oldState)
//                }
//            )
//        }
        AudioControl(
            modifier = Modifier
                .align(CenterHorizontally)
                .padding(24.dp)
                .size(64.dp),
            discState = discAnimationState.value
        ) {
            if (localState !is UiState.Success ||
                localState.data.album == null
            ) return@AudioControl // todo handle ui state of error
            viewModel.playPauseToggle()
        }
    }
}