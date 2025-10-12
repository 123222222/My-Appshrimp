package com.dung.myapplication.mainUI.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF42A5F5),          // Blue nhạt
    onPrimary = Color.White,

    primaryContainer = Color(0xFF90CAF9), // container xanh nhạt hơn
    onPrimaryContainer = Color(0xFF0D47A1),

    background = Color(0xFFFFFFFF),       // nền trắng
    onBackground = Color(0xFF000000),

    surface = Color(0xFFFFFFFF),          // card, appbar = trắng
    onSurface = Color(0xFF1C1B1F),

    surfaceVariant = Color(0xFFE3F2FD),   // xanh siêu nhạt
    onSurfaceVariant = Color(0xFF455A64),

    secondary = Color(0xFF64B5F6),
    onSecondary = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color.Black,
    background = Color(0xFFF8F6F6),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF37474F),
    onSurfaceVariant = Color(0xFFB0BEC5)
)



@Composable
fun MyKhoaLuanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}