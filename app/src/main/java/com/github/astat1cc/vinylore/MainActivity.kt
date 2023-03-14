package com.github.astat1cc.vinylore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.github.astat1cc.vinylore.core.theme.VinyloreTheme
import com.github.astat1cc.vinylore.core.theme.brown
import com.github.astat1cc.vinylore.core.theme.darkBackground
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
//        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            VinyloreTheme {
                val systemUiController = rememberSystemUiController()
                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = darkBackground
                    )
                    systemUiController.setNavigationBarColor(
                        color = darkBackground
                    )
                }
                ApplicationScreen()
            }
        }
    }
}