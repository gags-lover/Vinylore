package com.github.astat1cc.vinylore

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset
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
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Composable
fun ApplicationScreen() {

    val navController = rememberAnimatedNavController()

    AnimatedNavHost(
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
        composable(
            route = NavigationTree.Player.name,
            enterTransition = null,
            exitTransition = null,
            popEnterTransition = null,
            popExitTransition = null
        ) {
            PlayerScreen(navController)
        }
        composable(
            route = NavigationTree.TrackList.name,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it  },
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        visibilityThreshold = IntOffset.VisibilityThreshold
                    )
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        visibilityThreshold = IntOffset.VisibilityThreshold
                    )
                )
            }
        ) {
            TrackListScreen(navController = navController)
        }
    }
}

const val LAST_OPENED_ALBUM_ID = "-1"
const val PLAYER_SCREEN_ARG = "player_arg"
const val TRACK_LIST_SCREEN_ARG = "track_list_arg"