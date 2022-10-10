package com.github.astat1cc.vinylore

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.astat1cc.vinylore.navigation.NavigationTree
import com.github.astat1cc.vinylore.player.ui.vinyl.compose.PlayerScreen
import com.github.astat1cc.vinylore.tracklist.ui.TrackListScreen

@Composable
fun ApplicationScreen() {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = NavigationTree.TrackList.name) {
        composable(NavigationTree.TrackList.name) { TrackListScreen(navController) }
        composable("${NavigationTree.Player.name}/{albumId}") { backStackEntry ->
            PlayerScreen(
                navController,
                backStackEntry.arguments?.getString("albumId")?.toInt() ?: -1 // todo handle id
            )
        }
    }
}