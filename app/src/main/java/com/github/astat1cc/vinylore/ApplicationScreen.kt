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
import com.github.astat1cc.vinylore.navigation.NavigationTree
import com.github.astat1cc.vinylore.player.ui.PlayerScreen
import com.github.astat1cc.vinylore.tracklist.ui.TrackListScreen

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ApplicationScreen() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "${NavigationTree.Player.name}/{$PLAYER_ARG}"
    ) {
        dialog(
            NavigationTree.TrackList.name,
            dialogProperties = DialogProperties(
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) { TrackListScreen(navController) }
        composable(
            route = "${NavigationTree.Player.name}/{$PLAYER_ARG}",
            arguments = listOf(navArgument(PLAYER_ARG) {
                type = NavType.StringType
                defaultValue = LAST_OPENED_ALBUM_ID
            })
        ) { backStackEntry ->
            PlayerScreen(
                navController,
                backStackEntry.arguments?.getString(PLAYER_ARG)?.toInt() ?: LAST_OPENED_ALBUM_ID
            )
        }
    }
}

const val LAST_OPENED_ALBUM_ID = -1
const val PLAYER_ARG = "player_arg"