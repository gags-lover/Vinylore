package com.github.astat1cc.vinylore.albumlist.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
import com.github.astat1cc.vinylore.core.theme.brown
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
    val clickedAlbumUri = viewModel.clickedAlbumUri.collectAsState()

    val configuration = LocalConfiguration.current
    val screenDensity = configuration.densityDpi / 160f
    val screenWidth = configuration.screenWidthDp * screenDensity

    val localCoroutineScope = rememberCoroutineScope()

    val contentResolver = LocalContext.current.contentResolver
    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    val dirChosenListener: (Uri) -> Unit = { chosenUri ->
        viewModel.handleChosenDirUri(chosenUri)
        viewModel.enableAlbumsScan()
    }
    val getDirLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { chosenDirUri ->
            if (chosenDirUri == null) return@rememberLauncherForActivityResult
            contentResolver.takePersistableUriPermission(chosenDirUri, takeFlags)
            dirChosenListener(chosenDirUri)
        }

    val albumClickIsProcessing = remember { mutableStateOf(false) }
    fun onAlbumClick(albumUri: Uri) {
        if (albumClickIsProcessing.value) return
        albumClickIsProcessing.value = true
        localCoroutineScope.launch {
            viewModel.onAlbumClick(albumUri).join()
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
                modifier = Modifier
                    .fillMaxSize()
                    .background(brown),
                contentAlignment = Alignment.Center
            ) {
                ChoseDirectoryButton(
                    messageText = localState.message,
                    buttonText = stringResource(R.string.try_again),
                    getDirLauncher = getDirLauncher
                )
            }
        }
        is UiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brown),
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
                modifier = Modifier
                    .fillMaxSize()
                    .background(brown),
                contentAlignment = Alignment.Center
            ) {
                when {
                    localState.data == null -> {
                        ChoseDirectoryButton(
                            messageText = stringResource(id = R.string.you_should_chose_dir),
                            buttonText = stringResource(id = R.string.chose_dir),
                            getDirLauncher = getDirLauncher
                        )
                    }
                    localState.data.isEmpty() -> {
                        Text(
                            text = stringResource(id = R.string.dir_is_empty),
                            color = vintagePaper // todo make normal style
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                AlbumListHeader(
                                    refreshButtonListener = { viewModel.enableAlbumsScan() },
                                    getDirLauncher = getDirLauncher
                                )
                                LazyColumn(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
//                                    contentPadding = PaddingValues(
//                                        top = 20.dp,
//                                        bottom = 20.dp
//                                    )
                                ) {
                                    viewModel.disableAlbumsScan()
                                    items(localState.data) { album ->
                                        AlbumView(
                                            album,
                                            onClick = { albumUri ->
                                                onAlbumClick(albumUri)
                                            },
                                            clickedAlbumUri = clickedAlbumUri.value,
                                            screenWidth = screenWidth.toInt()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}