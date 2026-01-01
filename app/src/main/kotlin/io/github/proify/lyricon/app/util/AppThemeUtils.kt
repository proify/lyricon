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

package io.github.proify.lyricon.app.util

import android.content.Context
import io.github.proify.lyricon.app.util.Utils.commitEdit
import io.github.proify.lyricon.app.util.Utils.getDefaultSharedPreferences

object AppThemeUtils {
    const val MODE_SYSTEM = 0
    const val MODE_LIGHT = 1
    const val MODE_DARK = 2

    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_MONET_COLOR = "theme_monet_color"

    fun getMode(context: Context): Int =
        getDefaultSharedPreferences(context).getInt(KEY_THEME_MODE, MODE_SYSTEM)

    fun setMode(context: Context, mode: Int) {
        getDefaultSharedPreferences(context).commitEdit { putInt(KEY_THEME_MODE, mode) }
    }

    fun isEnableMonetColor(context: Context): Boolean =
        getDefaultSharedPreferences(context).getBoolean(
            KEY_MONET_COLOR,
            false
        )

    fun setEnableMonetColor(context: Context, enable: Boolean) {
        getDefaultSharedPreferences(context).commitEdit { putBoolean(KEY_MONET_COLOR, enable) }
    }

}