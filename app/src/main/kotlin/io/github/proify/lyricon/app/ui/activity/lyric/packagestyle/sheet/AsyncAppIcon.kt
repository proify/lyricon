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
fun AsyncAppIcon(application: ApplicationInfo, modifier: Modifier = Modifier) {
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