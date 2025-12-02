package com.dung.myapplication

import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.dung.myapplication.mainUI.NavGraph
import com.dung.myapplication.mainUI.theme.MyKhoaLuanTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {
            MyKhoaLuanTheme {
                HideBars()
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}

@Composable
fun HideBars() {
    val systemUiController = rememberSystemUiController()
    LaunchedEffect(Unit) {
        systemUiController.isStatusBarVisible = true
        systemUiController.isNavigationBarVisible = false
        systemUiController.systemBarsBehavior =
            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
