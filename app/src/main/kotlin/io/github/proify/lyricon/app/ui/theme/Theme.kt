/*
 * Lyricon – An Xposed module that extends system functionality
 * Copyright (C) 2026 Proify
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.proify.lyricon.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import io.github.proify.lyricon.app.util.AppThemeUtils
import top.yukonga.miuix.kmp.theme.Colors
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import top.yukonga.miuix.kmp.theme.platformDynamicColors


@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val window = (view.context as Activity).window

    val color = getThemeColorScheme()
    SideEffect {
        WindowInsetsControllerCompat(window, view)
            .isAppearanceLightStatusBars = color.isDark.not()
    }
    MiuixTheme(
        colors = color.colors,
    ) {
        content()
    }
}

@Composable
fun getThemeColorScheme(): AppColors {
    val context = LocalContext.current
    val isEnableMonet = AppThemeUtils.isEnableMonetColor(context)

    val dark = when (AppThemeUtils.getMode(context)) {
        AppThemeUtils.MODE_LIGHT -> false
        AppThemeUtils.MODE_DARK -> true
        else -> isSystemInDarkTheme()
    }

    if (isEnableMonet) {
        return AppColors(platformDynamicColors(dark), dark)
    }
    if (dark) {
        return AppColors(appDarkColorScheme(), dark)
    } else {
        return AppColors(appLightColorScheme(), dark)
    }

}

class AppColors(
    val colors: Colors,
    val isDark: Boolean
)

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