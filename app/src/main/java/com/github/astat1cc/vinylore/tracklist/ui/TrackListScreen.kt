package com.github.astat1cc.vinylore.tracklist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.core.models.ui.UiState
import com.github.astat1cc.vinylore.core.theme.brown
import com.github.astat1cc.vinylore.core.theme.vintagePaper
import com.github.astat1cc.vinylore.navigation.NavigationTree
import com.github.astat1cc.vinylore.tracklist.ui.views.TrackView
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TrackListScreen(
    navController: NavHostController,
    viewModel: TrackListScreenViewModel = getViewModel()
) {
    val uiState = viewModel.uiState.collectAsState()

    Column(modifier = Modifier.background(vintagePaper)) {
//        Box(modifier = Modifier.fillMaxWidth().background(brown)) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = null,
            tint = brown,
            modifier = Modifier
                .padding(start = 4.dp, top = 4.dp)
                .align(Alignment.Start)
                .clip(CircleShape)
                .clickable(onClick = {
                    navController.navigateUp()
                })
                .size(48.dp)
                .padding(12.dp)
        )
//        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (val localState = uiState.value) {
                is UiState.Success -> {
                    LazyColumn(contentPadding = PaddingValues(vertical = 20.dp)) {
                        val trackList = localState.data.trackList
                        items(trackList) { track ->
                            TrackView(track)
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