package com.github.astat1cc.vinylore

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.github.astat1cc.vinylore.core.theme.brown
import com.github.astat1cc.vinylore.navigation.NavigationTree
import com.github.astat1cc.vinylore.player.ui.PlayerScreen
import com.github.astat1cc.vinylore.tracklist.ui.TrackListScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ApplicationScreen() {

    val navController = rememberAnimatedNavController()

    AnimatedNavHost(
        modifier = Modifier.background(Color.Black),
        navController = navController,
        startDestination = NavigationTree.Player.name
    ) {
//        dialog(
//            route = NavigationTree.AlbumList.name,
//            dialogProperties = DialogProperties(
//                dismissOnClickOutside = true,
//                usePlatformDefaultWidth = false
//            )
//        ) { AlbumListScreen(navController) }
        composable(
            route = NavigationTree.AlbumList.name,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(
                        durationMillis = SLIDE_IN_DURATION,
                        easing = LinearOutSlowInEasing
                    )
                ) + scaleIn(
                    initialScale = 0.2f,
                    animationSpec = tween(
                        durationMillis = SLIDE_IN_DURATION,
                        easing = LinearOutSlowInEasing
                    )
                )
//                    spring(
//                        stiffness = Spring.StiffnessMediumLow,
//                    )
//                ) + fadeIn(initialAlpha = 1f)
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(
                        durationMillis = SLIDE_OUT_DURATION,
                        easing = FastOutLinearInEasing
                    )
                ) + scaleOut(
                    targetScale = 0.2f,
                    animationSpec = tween(
                        durationMillis = SLIDE_OUT_DURATION,
                        easing = FastOutLinearInEasing
                    )
                )
//                    spring(
//                        stiffness = Spring.StiffnessMediumLow,
//                    )
//                ) + fadeOut(targetAlpha = 1f)
            },
            exitTransition = { fadeOut(targetAlpha = 1f) }
        ) { AlbumListScreen(navController) }
        composable(
            route = NavigationTree.Player.name,
            enterTransition = { fadeIn(initialAlpha = 1f) },
            popEnterTransition = { fadeIn(initialAlpha = 0f) },
            exitTransition = { fadeOut(targetAlpha = 0.5f) }
        ) {
            PlayerScreen(navController)
        }
        composable(
            route = NavigationTree.TrackList.name,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(
                        durationMillis = SLIDE_IN_DURATION,
                        easing = LinearOutSlowInEasing
                    )
                ) + scaleIn(
                    initialScale = 0.2f,
                    animationSpec = tween(
                        durationMillis = SLIDE_IN_DURATION,
                        easing = LinearOutSlowInEasing
                    )
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(
                        durationMillis = SLIDE_OUT_DURATION,
                        easing = FastOutLinearInEasing
                    )
                ) + scaleOut(
                    targetScale = 0.2f,
                    animationSpec = tween(
                        durationMillis = SLIDE_OUT_DURATION,
                        easing = FastOutLinearInEasing
                    )
                )
            },
//            popEnterTransition = { fadeIn(initialAlpha = 1f) }
        ) {
            TrackListScreen(navController = navController)
        }
    }
}

const val SLIDE_IN_DURATION = 350
const val SLIDE_OUT_DURATION = 250