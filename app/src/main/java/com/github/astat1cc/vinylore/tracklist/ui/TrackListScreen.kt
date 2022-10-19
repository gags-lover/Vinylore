package com.github.astat1cc.vinylore.tracklist.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.navigation.NavigationTree
import com.github.astat1cc.vinylore.tracklist.ui.models.UiState
import com.github.astat1cc.vinylore.tracklist.ui.views.AlbumView
import com.github.astat1cc.vinylore.tracklist.ui.views.ChoseDirectoryView
import org.koin.androidx.compose.getViewModel

@Composable
fun TrackListScreen(
    navController: NavHostController,
    viewModel: TrackListScreenViewModel = getViewModel()
) {
    val uiState = viewModel.uiState.collectAsState() // todo check if ui is recomposing after the same data is collected

    when (uiState.value) {
        is UiState.Success -> {
            val successState = uiState.value as? UiState.Success ?: return // todo
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    successState.data == null -> {
                        ChoseDirectoryView(
                            messageText = stringResource(id = R.string.you_should_chose_dir),
                            buttonText = stringResource(id = R.string.chose_dir),
                            dirChosenListener = { chosenUri ->
                                viewModel.handleChosenDirUri(chosenUri)
                            }
                        )
                    }
                    successState.data.isEmpty() -> {
                        Text(text = stringResource(id = R.string.dir_is_empty))
                    }
                    else -> {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            contentPadding = PaddingValues(start = 8.dp, end = 24.dp)
                        ) {
                            items(successState.data) { album ->
                                AlbumView(album, onClick = { albumId ->
                                    navController.navigate("${NavigationTree.Player.name}/$albumId")
                                })
                            }
                        }
                    }
                }
            }

        }
        is UiState.Fail -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val failState = uiState.value as? UiState.Fail ?: return // todo

                ChoseDirectoryView(
                    messageText = failState.message,
                    buttonText = stringResource(R.string.try_again),
                    dirChosenListener = { chosenUri ->
                        viewModel.handleChosenDirUri(chosenUri)
                    }
                )
            }
        }
        is UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

