package com.github.astat1cc.vinylore.albumlist.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import com.github.astat1cc.vinylore.R
import com.github.astat1cc.vinylore.albumlist.ui.views.AlbumListHeader
import com.github.astat1cc.vinylore.navigation.NavigationTree
import com.github.astat1cc.vinylore.core.models.ui.UiState
import com.github.astat1cc.vinylore.albumlist.ui.views.AlbumView
import com.github.astat1cc.vinylore.albumlist.ui.views.ChoseDirectoryButton
import com.github.astat1cc.vinylore.core.theme.vintagePaper
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

// todo indicate already chosen album
// todo when u chose already chosen album don't stop playing
// todo scrolling bar

@Composable
fun AlbumListScreen(
    navController: NavHostController,
    viewModel: AlbumListScreenViewModel = getViewModel()
) {
    val uiState = viewModel.uiState.collectAsState()
    val localCoroutineScope = rememberCoroutineScope()

    val albumClickProcessing = remember { mutableStateOf(false) }
    fun onAlbumClick(albumId: Int) {
        if (albumClickProcessing.value) return
        albumClickProcessing.value = true
        localCoroutineScope.launch {
            viewModel.saveChosenPlayingAlbum(albumId).join()
            // todo add animation: mockup going to the right to indicate where track list is now
            navController.navigate(
                NavigationTree.Player.name,
                NavOptions.Builder().setPopUpTo(NavigationTree.Player.name, true)
                    .build()
            )
        }
    }


    when (val localState = uiState.value) {
        is UiState.Fail -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ChoseDirectoryButton(
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = vintagePaper)
                        Text(
                            modifier = Modifier.padding(top = 8.dp),
                            text = stringResource(R.string.scanning_folders),
                            color = vintagePaper,
                            fontSize = 18.sp,
                        )
                    }
                }
            }
        }
        is UiState.Success -> {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    localState.data == null -> {
                        ChoseDirectoryButton(
                            messageText = stringResource(id = R.string.you_should_chose_dir),
                            buttonText = stringResource(id = R.string.chose_dir),
                            dirChosenListener = { chosenUri ->
                                viewModel.handleChosenDirUri(chosenUri)
                            }
                        )
                    }
                    localState.data.isEmpty() -> {
                        Text(
                            text = stringResource(id = R.string.dir_is_empty),
                            color = vintagePaper // todo make normal style
                        )
                    }
                    else -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AlbumListHeader(
                                refreshButtonListener = { viewModel.enableAlbumsScan() }
                            )
                            LazyRow(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                contentPadding = PaddingValues(start = 8.dp, end = 24.dp)
                            ) {
                                viewModel.disableAlbumsScan()
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
        }
    }
}