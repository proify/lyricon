package io.github.proify.lyricon.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.Colors
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    MiuixTheme(

        colors = if (isSystemInDarkTheme()) appDarkColorScheme() else appLightColorScheme()
    ) {
        content()
    }
}

fun appDarkColorScheme() = darkColorScheme(
    error = Color(0xFFF44336),
    errorContainer = Color(0xFFEF9A9A),
)

fun appLightColorScheme(): Colors {
    val black = Color(0xFF111111)
    return lightColorScheme(
        surface = Color(0xFFF0F1F2),
        onBackground = black,
        onSurface = black,
        onSurfaceContainer = black,
        error = Color(0xFFef5350),
        errorContainer = Color(0xFFEF9A9A),
    )
}