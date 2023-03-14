package com.github.astat1cc.vinylore.player.ui

import android.content.res.Configuration
import android.util.Log
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
    val albumChoosingCalled = viewModel.albumChoosingCalled.collectAsState()
    val discAnimationState = viewModel.playerAnimationState.collectAsState()
    val tonearmAnimationState = viewModel.tonearmAnimationState.collectAsState()
    val discRotation = viewModel.discRotation.collectAsState()
    val tonearmRotation = viewModel.tonearmRotation.collectAsState()
    val currentPlayingTrack = viewModel.currentPlayingTrack.collectAsState()
    val trackProgress = viewModel.currentTrackProgress.collectAsState()
    val tonearmLifted = viewModel.tonearmLifted.collectAsState()
    val albumPreparedRecently = viewModel.albumPreparedRecently.collectAsState()
    val shouldRefreshScreen = viewModel.shouldRefreshScreen.collectAsState(initial = false)

    var refreshCalled by remember { mutableStateOf(false) }
    if (shouldRefreshScreen.value && !refreshCalled) {
        Log.e("service", "${shouldRefreshScreen.value} vm: ${viewModel.hashCode()}")
        refreshCalled = true
        navController.navigate(NavigationTree.Player.name) {
            viewModel.refreshCalled()
            navController.popBackStack(
                NavigationTree.Player.name,
                inclusive = true
            )
        }
    }

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
                text = if (localState is UiState.Success) localState.data.album?.name?.uppercase()
                    ?: "" else "",
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
        if (orientationPortrait) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = CenterHorizontally
            ) {
                VinylPlayerView(
                    vinylSize = vinylSize,
                    discAnimationState = discAnimationState.value,
                    playerStateTransition = { oldState ->
                        viewModel.resumePlayerAnimationStateFrom(oldState)
                    },
                    discRotation = discRotation.value,
                    changeVinylRotation = { newRotation ->
                        viewModel.changeDiscRotationFromAnimation(newRotation)
                    },
                    playingTrack = currentPlayingTrack.value,
                    tonearmRotation = tonearmRotation.value,
                    tonearmAnimationState = tonearmAnimationState.value,
                    tonearmLifted = tonearmLifted.value,
                    tonearmTransition = { oldState ->
                        viewModel.resumeTonearmAnimationStateFrom(oldState)
                    },
                    changeTonearmRotation = { newRotation ->
                        viewModel.changeTonearmRotationFromAnimation(newRotation)

                    },
                    shouldShowVinylAppearanceAnimation = albumPreparedRecently.value,
                    vinylAppearanceAnimationShown = {
                        viewModel.vinylAppearanceAnimationShown()
                    },
                    orientationPortrait = orientationPortrait
                )
                TrackInfoAndControlView(
                    modifier = Modifier.weight(1f),
//                    playingTrackName = currentPlayingTrack.value?.let { track ->
//                        track.artist + " — " + track.title
//                    } ?: "",
                    title = currentPlayingTrack.value?.title ?: "",
                    artist = currentPlayingTrack.value?.artist ?: "",
                    trackProgress = trackProgress.value,
                    sliderDraggingFinished = { viewModel.sliderDraggingFinished() },
                    sliderDragging = { newValue ->
                        viewModel.sliderDragging(newValue)
                    },
                    tonearmRotation = tonearmRotation.value,
                    discAnimationState = discAnimationState.value,
                    togglePlayPause = {
                        if (localState is UiState.Success &&
                            localState.data.album != null
                        ) { // todo handle ui state of error
                            viewModel.playPauseToggle()
                        }
                    },
                    skipToPrevious = { viewModel.skipToPrevious() },
                    skipToNext = { viewModel.skipToNext() },
                )
            }
        } else {
            Row(
                modifier = Modifier
//                content = playerScreenItems,
            ) {
                VinylPlayerView(
                    vinylSize = vinylSize,
                    discAnimationState = discAnimationState.value,
                    playerStateTransition = { oldState ->
                        viewModel.resumePlayerAnimationStateFrom(oldState)
                    },
                    discRotation = discRotation.value,
                    changeVinylRotation = { newRotation ->
                        viewModel.changeDiscRotationFromAnimation(newRotation)
                    },
                    playingTrack = currentPlayingTrack.value,
                    tonearmRotation = tonearmRotation.value,
                    tonearmAnimationState = tonearmAnimationState.value,
                    tonearmLifted = tonearmLifted.value,
                    tonearmTransition = { oldState ->
                        viewModel.resumeTonearmAnimationStateFrom(oldState)
                    },
                    changeTonearmRotation = { newRotation ->
                        viewModel.changeTonearmRotationFromAnimation(newRotation)

                    },
                    shouldShowVinylAppearanceAnimation = albumPreparedRecently.value,
                    vinylAppearanceAnimationShown = {
                        viewModel.vinylAppearanceAnimationShown()
                    },
                    orientationPortrait = orientationPortrait
                )
                TrackInfoAndControlView(
                    modifier = Modifier
                        .weight(1f),
//                    playingTrackName = currentPlayingTrack.value?.title ?: "",
                    trackProgress = trackProgress.value,
                    sliderDraggingFinished = { viewModel.sliderDraggingFinished() },
                    sliderDragging = { newValue ->
                        viewModel.sliderDragging(newValue)
                    },
                    tonearmRotation = tonearmRotation.value,
                    discAnimationState = discAnimationState.value,
                    togglePlayPause = {
                        if (localState is UiState.Success &&
                            localState.data.album != null
                        ) { // todo handle ui state of error
                            viewModel.playPauseToggle()
                        }
                    },
                    skipToPrevious = { viewModel.skipToPrevious() },
                    skipToNext = { viewModel.skipToNext() },
                    title = currentPlayingTrack.value?.title ?: "",
                    artist = currentPlayingTrack.value?.artist ?: "",
                )
            }
        }
    }

    // track preparing loading view
    if (localState is UiState.Loading
        || currentPlayingTrack.value == null
    ) {
        Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
//                    .background(color = dimBackground)
                ,
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