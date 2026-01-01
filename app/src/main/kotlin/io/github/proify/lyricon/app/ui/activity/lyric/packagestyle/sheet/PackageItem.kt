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