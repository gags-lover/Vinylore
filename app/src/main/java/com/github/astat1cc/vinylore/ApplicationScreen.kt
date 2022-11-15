package com.github.astat1cc.vinylore

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.github.astat1cc.vinylore.albumlist.ui.AlbumListScreen
import com.github.astat1cc.vinylore.navigation.NavigationTree
import com.github.astat1cc.vinylore.player.ui.PlayerScreen
import com.github.astat1cc.vinylore.tracklist.ui.TrackListScreen

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ApplicationScreen() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavigationTree.Player.name
    ) {
        dialog(
            route = NavigationTree.AlbumList.name,
            dialogProperties = DialogProperties(
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) { AlbumListScreen(navController) }
        composable(route = NavigationTree.Player.name) {
            PlayerScreen(navController)
        }
        composable(route = NavigationTree.TrackList.name) {
            TrackListScreen(navController = navController)
        }
    }
}

const val LAST_OPENED_ALBUM_ID = "-1"
const val PLAYER_SCREEN_ARG = "player_arg"
const val TRACK_LIST_SCREEN_ARG = "track_list_arg"