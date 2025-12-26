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