package com.github.astat1cc.vinylore.tracklist.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.models.ui.UiState
import com.github.astat1cc.vinylore.core.theme.darkBackground
import com.github.astat1cc.vinylore.core.theme.vintagePaper
import com.github.astat1cc.vinylore.navigation.NavigationTree
import com.github.astat1cc.vinylore.tracklist.ui.views.TrackView
import org.koin.androidx.compose.getViewModel

//todo handle if empty list maybe

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TrackListScreen(
    navController: NavHostController,
    viewModel: TrackListScreenViewModel = getViewModel()
) {
    val uiState = viewModel.uiState.collectAsState()
    val shouldNavigateToPlayer = viewModel.shouldNavigateToPlayer.collectAsState(initial = false)
    val localState = uiState.value

    if (shouldNavigateToPlayer.value) {
        navController.navigate(
            NavigationTree.Player.name,
            NavOptions.Builder().setPopUpTo(NavigationTree.Player.name, true)
                .build()
        )
    }

    Column(modifier = Modifier.background(vintagePaper)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // back button
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = null,
                tint = darkBackground,
                modifier = Modifier
                    .padding(4.dp)
                    .align(Alignment.CenterStart)
                    .clip(CircleShape)
                    .clickable(onClick = {
                        navController.navigateUp()
                    })
                    .size(48.dp)
                    .padding(12.dp)
            )
            // Track list text
            Text(
                text = stringResource(R.string.track_list),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                maxLines = 1,
                color = Color.Black,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .align(Alignment.Center),
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
            )
        }
        // track list
        androidx.compose.animation.AnimatedVisibility(
            visible = localState is UiState.Success,
            enter = fadeIn(),
            exit = fadeOut() // doesn't get to it anyway
        ) {
            if (localState !is UiState.Success) return@AnimatedVisibility
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(contentPadding = PaddingValues(vertical = 20.dp)) {
                    val album = localState.data.album!!
                    itemsIndexed(album.trackList) { index, track ->
                        TrackView(
                            track = track,
                            trackPosition = index + 1,
                            isCurrentlyPlaying = track == localState.data.currentPlayingTrack,
                            useTitleWithoutArtist = album.albumOfOneArtist,
                            onTrackViewClicked = {
                                viewModel.skipToQueueItem(
                                    album.trackList.indexOf(track).toLong(),
                                    track
                                )
                            }
                        )
                    }
                }
            }
        }
        when (localState) {
            // loading
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = darkBackground)
                }
            }
            else -> {} // todo fail
        }
    }
}