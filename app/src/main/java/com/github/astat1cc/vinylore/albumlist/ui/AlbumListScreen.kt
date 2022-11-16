package com.github.astat1cc.vinylore.albumlist.ui

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
import androidx.navigation.NavOptions
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.navigation.NavigationTree
import com.github.astat1cc.vinylore.core.models.ui.UiState
import com.github.astat1cc.vinylore.albumlist.ui.views.AlbumView
import com.github.astat1cc.vinylore.albumlist.ui.views.ChoseDirectoryView
import com.github.astat1cc.vinylore.core.theme.vintagePaper
import org.koin.androidx.compose.getViewModel

@Composable
fun AlbumListScreen(
    navController: NavHostController,
    viewModel: AlbumListScreenViewModel = getViewModel()
) {
    val uiState = viewModel.uiState.collectAsState()

    fun onAlbumClick(albumId: Int) {
        viewModel.saveChosenPlayingAlbum(albumId)
        // todo add animation: mockup going to the right to indicate where track list is now
        navController.navigate(
            NavigationTree.Player.name,
            NavOptions.Builder().setPopUpTo(NavigationTree.Player.name, true).build()
        )
    }
    when (val localState = uiState.value) {
        is UiState.Success -> {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    localState.data == null -> {
                        ChoseDirectoryView(
                            messageText = stringResource(id = R.string.you_should_chose_dir),
                            buttonText = stringResource(id = R.string.chose_dir),
                            dirChosenListener = { chosenUri ->
                                viewModel.handleChosenDirUri(chosenUri)
                            }
                        )
                    }
                    localState.data.isEmpty() -> {
                        Text(text = stringResource(id = R.string.dir_is_empty))
                    }
                    else -> {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            contentPadding = PaddingValues(start = 8.dp, end = 24.dp)
                        ) {
                            items(localState.data) { album ->
                                AlbumView(album, onClick = { albumId ->
                                    onAlbumClick(albumId)
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
                ChoseDirectoryView(
                    messageText = localState.message,
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
                CircularProgressIndicator(color = vintagePaper)
            }
        }
    }
}



