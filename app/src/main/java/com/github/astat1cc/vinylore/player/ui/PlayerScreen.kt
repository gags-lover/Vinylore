package com.github.astat1cc.vinylore.player.ui

import android.content.res.Configuration
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.models.ui.UiState
import com.github.astat1cc.vinylore.core.theme.*
import com.github.astat1cc.vinylore.navigation.NavigationTree
import com.github.astat1cc.vinylore.player.ui.views.TrackInfoAndControlView
import com.github.astat1cc.vinylore.player.ui.views.VinylPlayerView
import org.koin.androidx.compose.getViewModel

// todo handle sorting track list
// todo handle bluetooth headphones delay
// todo extra spin when pause pressed
// todo make tonearm move slowly to start position while going to next track
// todo handle autofocus pause other media services
// todo when first time chose album albumscreen shows again
// todo handle error states

@Composable
fun PlayerScreen(
    navController: NavHostController,
    viewModel: PlayerScreenViewModel = getViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val uiState = viewModel.uiState.collectAsState()
    val shouldNavigateToAlbumChoosing = viewModel.shouldNavigateToAlbumChoosing.collectAsState()
    val vinylAnimationState = viewModel.vinylAnimationState.collectAsState()
    val tonearmAnimationState = viewModel.tonearmAnimationState.collectAsState()
    val discRotation = viewModel.vinylRotation.collectAsState()
    val tonearmRotation = viewModel.tonearmRotation.collectAsState()
    val currentPlayingTrack = viewModel.currentPlayingTrack.collectAsState()
    val trackProgress = viewModel.currentTrackProgress.collectAsState()
    val tonearmLifted = viewModel.tonearmLifted.collectAsState()
    val showVinyl = viewModel.showVinyl.collectAsState()
    val shouldRefreshScreen = viewModel.shouldRefreshScreen.collectAsState(initial = false)
    val showPlayIconAtPlayPauseToggle = viewModel.showPlayIconAtPlayPauseToggle.collectAsState()
    val playPauseToggleBlocked = viewModel.playPauseToggleBlocked.collectAsState()
    val sliderEnabled = viewModel.sliderEnabled.collectAsState(initial = false)

//    var refreshCalled by remember { mutableStateOf(false) }
    if (shouldRefreshScreen.value) {
//        refreshCalled = true // todo
        viewModel.refreshUsed()
        navController.navigate(NavigationTree.Player.name) {
//            viewModel.refreshCalled()
            navController.popBackStack(
                NavigationTree.Player.name,
                inclusive = true
            )
        }
    }

    val localState = uiState.value
    // open album choosing screen if currently no album is chosen (happens only once, then user should
    // click album choosing button manually
    if (shouldNavigateToAlbumChoosing.value &&
        localState is UiState.Fail
    ) {
        viewModel.albumChoosingCalled()
        navController.navigate(NavigationTree.AlbumList.name)
    }
    val configuration = LocalConfiguration.current
    val orientationPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    val vinylSize = if (orientationPortrait) {
        (configuration.screenWidthDp.dp - 32.dp) * 0.7f  // 32 dp is padding 16 + 16 left and right
    } else {
        (configuration.screenHeightDp.dp - 32.dp) * 0.7f
    }

    // sending information every time screen is visible or not to decide to show animation or not
    DisposableEffect(lifecycleOwner) {
        val visibilityObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.shouldShowSmoothStartAndStopVinylAnimation()
            } else if (event == Lifecycle.Event.ON_STOP) {
                viewModel.shouldNotShowSmoothStartAndStopVinylAnimation()
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
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(darkBackground)
        ) {
            // folder choosing view
            Icon(
                painter = painterResource(R.drawable.ic_folder),
                contentDescription = null,
                tint = MaterialTheme.colors.onPrimary,
                modifier = Modifier
                    .padding(4.dp)
                    .align(CenterVertically)
                    .clip(CircleShape)
                    .clickable(onClick = {
                        navController.navigate(NavigationTree.AlbumList.name) {
                            navController.popBackStack(
                                NavigationTree.AlbumList.name,
                                inclusive = true
                            )
                        }
                    })
                    .size(48.dp)
                    .padding(12.dp)
            )
            // album name
            Text(
                text = if (localState is UiState.Success) localState.data.name.uppercase() else "",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                maxLines = 1,
                color = Color.White,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .align(CenterVertically),
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
            )
            // track list view
            Icon(
                painter = painterResource(id = R.drawable.ic_list),
                contentDescription = null, // todo
                tint = MaterialTheme.colors.onPrimary,
                modifier = Modifier
                    .padding(4.dp)
                    .align(CenterVertically)
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

        @Composable
        fun VinylPlayerViewItem() = VinylPlayerView(
            vinylSize = vinylSize,
            discAnimationState = vinylAnimationState.value,
            playerStateTransition = { oldState ->
                viewModel.resumePlayerAnimationStateFrom(oldState)
            },
            discRotation = discRotation.value,
            changeVinylRotation = { newRotation ->
                viewModel.changeDiscRotationFromAnimation(newRotation)
            },
            playingTrack = if (localState is UiState.Loading) null else (currentPlayingTrack.value),
            tonearmRotation = tonearmRotation.value,
            tonearmAnimationState = tonearmAnimationState.value,
            tonearmLifted = tonearmLifted.value,
            tonearmTransition = { oldState ->
                viewModel.resumeTonearmAnimationStateFrom(oldState)
            },
            changeTonearmRotation = { newRotation ->
                viewModel.changeTonearmRotationFromAnimation(newRotation)
            },
            showVinyl = showVinyl.value,
            vinylAppearanceAnimationShown = {
                viewModel.vinylAppearanceAnimationShown()
            },
            orientationPortrait = orientationPortrait
        )

        @Composable
        fun TrackInfoAndControlViewItem() = TrackInfoAndControlView(
            modifier = Modifier
                .weight(1f),
//                    playingTrackName = currentPlayingTrack.value?.title ?: "",
            trackProgress = trackProgress.value,

            sliderDraggingFinished = { viewModel.sliderDraggingFinished() },
            sliderDragging = { newValue ->
                viewModel.dragSlider(newValue)
            },
            sliderEnabled = sliderEnabled.value,
            togglePlayPause = {
                viewModel.playPauseToggle()
            },
            skipToPrevious = {
                val position =
                    (localState as? UiState.Success)?.let { uiState ->
                        uiState.data.trackList.indexOf(
                            currentPlayingTrack.value
                        ).toLong()
                    }
                viewModel.skipToPrevious(currentTrackQueuePosition = position)
            },
            skipToNext = { viewModel.skipToNext() },
            playingTrack = if (localState is UiState.Loading) null else (currentPlayingTrack.value),
            isPlaying = !showPlayIconAtPlayPauseToggle.value,
            playPauseToggleBlocked = playPauseToggleBlocked.value,
            errorMessage = (localState as? UiState.Fail)?.message
        )
        if (orientationPortrait) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = CenterHorizontally
            ) {
                VinylPlayerViewItem()
                TrackInfoAndControlViewItem()
            }
        } else {
            Row {
                VinylPlayerViewItem()
                TrackInfoAndControlViewItem()
            }
        }
    }

    // track preparing loading view
    if (localState !is UiState.Fail &&
        (localState is UiState.Loading || currentPlayingTrack.value == null)
    ) {
        Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier.fillMaxSize() ,
                contentAlignment = Center
            ) {
                Column(horizontalAlignment = CenterHorizontally) {
                    CircularProgressIndicator(color = vintagePaper)
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = stringResource(R.string.preparing_media),
                        color = vintagePaper,
                        fontSize = 18.sp,
                    )
                }
            }
        }
    }
}