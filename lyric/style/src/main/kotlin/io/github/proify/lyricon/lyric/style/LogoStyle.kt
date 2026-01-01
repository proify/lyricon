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

package io.github.proify.lyricon.lyric.style

import android.content.SharedPreferences
import android.os.Parcelable
import io.github.proify.android.extensions.jsonx
import io.github.proify.android.extensions.safeDecode
import io.github.proify.android.extensions.toJson
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class LogoStyle(
    var enable: Boolean = Defaults.ENABLE,
    var style: Int = Defaults.STYLE,

    var enableCustomColor: Boolean = Defaults.ENABLE_CUSTOM_COLOR,
    var lightModeColor: LogoColor = Defaults.LIGHT_MODE_COLOR,
    var darkModeColor: LogoColor = Defaults.DARK_MODE_COLOR,

    var margins: RectF = Defaults.MARGINS,
    var hideInColorOSCapsuleMode: Boolean = Defaults.HIDE_IN_COLOROS_CAPSULE_MODE,

    var width: Float = Defaults.WIDTH,
    var height: Float = Defaults.HEIGHT
) : AbstractStyle(), Parcelable {

    override fun onLoad(preferences: SharedPreferences) {
        enable = preferences.getBoolean("lyric_style_logo_enable", Defaults.ENABLE)
        style = preferences.getInt("lyric_style_logo_style", Defaults.STYLE)

        enableCustomColor = preferences.getBoolean(
            "lyric_style_logo_enable_custom_color",
            Defaults.ENABLE_CUSTOM_COLOR
        )
        lightModeColor = jsonx.safeDecode<LogoColor>(
            preferences.getString("lyric_style_logo_color_light_mode", null),
            Defaults.LIGHT_MODE_COLOR
        )
        darkModeColor = jsonx.safeDecode<LogoColor>(
            preferences.getString("lyric_style_logo_color_dark_mode", null),
            Defaults.DARK_MODE_COLOR
        )

        margins = jsonx.safeDecode<RectF>(
            preferences.getString("lyric_style_logo_margins", null),
            Defaults.MARGINS
        )
        hideInColorOSCapsuleMode = preferences.getBoolean(
            "lyric_style_logo_hide_in_coloros_capsule_mode",
            Defaults.HIDE_IN_COLOROS_CAPSULE_MODE
        )

        width = preferences.getFloat("lyric_style_logo_width", Defaults.WIDTH)
        height = preferences.getFloat("lyric_style_logo_height", Defaults.HEIGHT)
    }

    override fun onWrite(editor: SharedPreferences.Editor) {
        editor.putBoolean("lyric_style_logo_enable", enable)
        editor.putInt("lyric_style_logo_style", style)

        editor.putBoolean("lyric_style_logo_enable_custom_color", enableCustomColor)
        editor.putString("lyric_style_logo_color_light_mode", lightModeColor.toJson())
        editor.putString("lyric_style_logo_color_dark_mode", darkModeColor.toJson())

        editor.putString("lyric_style_logo_margins", margins.toJson())
        editor.putBoolean("lyric_style_logo_hide_in_coloros_capsule_mode", hideInColorOSCapsuleMode)

        editor.putFloat("lyric_style_logo_width", width)
        editor.putFloat("lyric_style_logo_height", height)
    }

    fun color(lightMode: Boolean) = if (lightMode) lightModeColor else darkModeColor

    object Defaults {
        const val ENABLE: Boolean = true
        const val STYLE: Int = STYLE_DEFAULT

        const val ENABLE_CUSTOM_COLOR: Boolean = false
        val LIGHT_MODE_COLOR: LogoColor = LogoColor()
        val DARK_MODE_COLOR: LogoColor = LogoColor()

        val MARGINS: RectF = RectF(right = 4f)
        const val HIDE_IN_COLOROS_CAPSULE_MODE: Boolean = true

        const val WIDTH: Float = 0f
        const val HEIGHT: Float = 0f
    }

    companion object {
        const val STYLE_DEFAULT: Int = 0
        const val STYLE_COVER_SQUIRCLE: Int = 1
        const val STYLE_COVER_CIRCLE: Int = 2
    }
}