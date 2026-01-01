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

package io.github.proify.lyricon.app.bridge

import android.content.Context
import androidx.annotation.Keep
import com.highcapable.yukihookapi.YukiHookAPI
import io.github.proify.lyricon.common.Constants
import java.io.File

object Bridge {

    fun isModuleActive(): Boolean = YukiHookAPI.Status.isXposedModuleActive

    @Keep
    fun getPreferenceDirectory(context: Context): File {
        return File(context.filesDir, "preferences")
    }

    @Keep
    fun getPreferenceFile(context: Context, name: String): File {
        return File(getPreferenceDirectory(context), getPreferenceFormatedFileName(name))
    }

    fun getPreferenceFormatedFileName(name: String) = "${name}.json"

    object LyricStylePrefs {

        const val DEFAULT_PACKAGE_NAME = Constants.APP_PACKAGE_NAME

        const val PREF_NAME_BASE_STYLE = "baseLyricStyle"

        const val PREF_PACKAGE_STYLE_MANAGER = "packageStyleManager"

        const val KEY_ENABLED_PACKAGES = "enables"

        fun getPackageStylePreferenceName(packageName: String): String =
            "package_style_${packageName.replace(".", "_")}"

    }

}