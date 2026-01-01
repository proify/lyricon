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

package io.github.proify.lyricon.app.ui.activity.lyric.packagestyle.sheet

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AsyncAppIcon(
    application: ApplicationInfo,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val packageName = application.packageName

    var icon by remember(packageName) {
        mutableStateOf<Drawable?>(null)
    }

    LaunchedEffect(packageName) {
        val cached = AppCache.getCachedIcon(packageName)
        if (cached != null) {
            icon = cached
            return@LaunchedEffect
        }

        val loaded = withContext(Dispatchers.IO) {
            runCatching {
                packageManager.getApplicationIcon(application)
            }.getOrNull()
        }

        loaded?.let {
            AppCache.cacheIcon(application.packageName, it)
            icon = it
        }
    }

    if (icon != null) {
        Image(
            painter = rememberDrawablePainter(icon),
            contentDescription = packageName,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(
                color = MiuixTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
        )
    }
}