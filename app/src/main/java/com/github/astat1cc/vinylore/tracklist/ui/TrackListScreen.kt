package com.github.astat1cc.vinylore.tracklist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.models.ui.UiState
import com.github.astat1cc.vinylore.core.theme.brown
import com.github.astat1cc.vinylore.core.theme.vintagePaper
import com.github.astat1cc.vinylore.tracklist.ui.views.TrackView
import org.koin.androidx.compose.getViewModel

@Composable
fun TrackListScreen(
    navController: NavHostController,
    viewModel: TrackListScreenViewModel = getViewModel()
) {
    val uiState = viewModel.uiState.collectAsState()
    val isPlaying = viewModel.isPlaying.collectAsState()
    val localState = uiState.value

    Column(modifier = Modifier.background(vintagePaper)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // back button
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = null,
                tint = brown,
                modifier = Modifier
                    .padding(start = 4.dp, top = 4.dp)
//                    .align(Alignment.Start)
                    .clip(CircleShape)
                    .clickable(onClick = {
                        navController.navigateUp()
                    })
                    .size(48.dp)
                    .padding(12.dp)
            )
            if (localState is UiState.Success) {
                Text(
                    text = localState.data.album?.name ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    maxLines = 1,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(
//                            top = 8.dp,
                            start = 8.dp,
                            end = 16.dp,
//                            bottom = 20.dp
                        )
                        .fillMaxWidth()
                        .weight(1f),
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (localState) {
                is UiState.Success -> {
                    LazyColumn(contentPadding = PaddingValues(vertical = 20.dp)) {
                        item {

                        }
                        val trackList = localState.data.album!!.trackList
                        items(trackList) { track ->
                            TrackView(
                                track,
                                track == localState.data.currentPlayingTrack
                            )
                        }
                    }
                }
                is UiState.Fail -> {

                }
                is UiState.Loading -> {
                    CircularProgressIndicator(color = brown)
                }
            }
        }
    }
}