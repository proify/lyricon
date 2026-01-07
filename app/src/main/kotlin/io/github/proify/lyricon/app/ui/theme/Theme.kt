/*
 * Copyright 2026 Proify
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    val context = view.context
    val window = if (context is Activity) context.window else null

    val color = getThemeColorScheme()
    SideEffect {
        if (window != null) WindowInsetsControllerCompat(window, view)
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
        AppThemeUtils.MODE_SYSTEM -> isSystemInDarkTheme()
        else -> isSystemInDarkTheme()
    }
    return when {
        isEnableMonet -> AppColors(platformDynamicColors(dark), dark)
        dark -> AppColors(appDarkColorScheme(), true)
        else -> AppColors(appLightColorScheme(), false)
    }
}

class AppColors(
    val colors: Colors,
    val isDark: Boolean
)

fun appDarkColorScheme(): Colors = darkColorScheme(
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