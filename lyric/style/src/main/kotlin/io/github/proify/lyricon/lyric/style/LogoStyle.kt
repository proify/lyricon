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

@file:Suppress("KotlinConstantConditions")

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

    fun color(lightMode: Boolean): LogoColor = if (lightMode) lightModeColor else darkModeColor

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