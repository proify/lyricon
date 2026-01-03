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
import io.github.proify.lyricon.app.Application
import io.github.proify.lyricon.app.util.LyricPrefs.DEFAULT_PACKAGE_NAME

data class PackageItem(
    val applicationInfo: ApplicationInfo,
) {
    val isDefault: Boolean
        get() = applicationInfo.packageName == DEFAULT_PACKAGE_NAME

    fun getLabel(): String {
        val cached = AppCache.getCachedLabel(applicationInfo.packageName)
        if (cached != null) return cached

        val context = Application.instance
        val packageManager = context.packageManager
        val label = applicationInfo.loadLabel(packageManager).toString()
        AppCache.cacheLabel(applicationInfo.packageName, label)
        return label
    }
}