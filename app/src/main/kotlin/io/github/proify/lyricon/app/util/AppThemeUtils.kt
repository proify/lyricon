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

package io.github.proify.lyricon.app.util

import android.content.Context
import io.github.proify.lyricon.app.util.Utils.commitEdit
import io.github.proify.lyricon.app.util.Utils.getDefaultSharedPreferences

object AppThemeUtils {
    const val MODE_SYSTEM: Int = 0
    const val MODE_LIGHT: Int = 1
    const val MODE_DARK: Int = 2

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