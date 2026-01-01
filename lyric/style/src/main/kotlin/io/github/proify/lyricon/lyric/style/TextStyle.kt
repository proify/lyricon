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

@Serializable
@Parcelize
data class TextStyle(
    var textSize: Float = Defaults.TEXT_SIZE,
    var margins: RectF = Defaults.MARGINS,
    var paddings: RectF = Defaults.PADDINGS,
    var repeatOutput: Boolean = Defaults.REPEAT_OUTPUT,

    var fadingEdgeEnabled: Boolean = Defaults.FADING_EDGE_ENABLED,
    var fadingEdgeLength: Int = Defaults.FADING_EDGE_LENGTH,

    var enableCustomTextColor: Boolean = Defaults.ENABLE_CUSTOM_TEXT_COLOR,
    var lightModeColor: TextColor? = Defaults.LIGHT_MODE_COLOR,
    var darkModeColor: TextColor? = Defaults.DARK_MODE_COLOR,

    var typeFace: String? = Defaults.TYPE_FACE,
    var typeFaceBold: Boolean = Defaults.TYPE_FACE_BOLD,
    var typeFaceItalic: Boolean = Defaults.TYPE_FACE_ITALIC,
    var fontWeight: Int = Defaults.FONT_WEIGHT,

    var marqueeSpeed: Float = Defaults.MARQUEE_SPEED,
    var marqueeGhostSpacing: Float = Defaults.MARQUEE_GHOST_SPACING,
    var marqueeLoopDelay: Int = Defaults.MARQUEE_LOOP_DELAY,
    var marqueeDelayEnable: Boolean = Defaults.MARQUEE_DELAY_ENABLE,
    var marqueeRepeatCount: Int = Defaults.MARQUEE_REPEAT_COUNT,
    var marqueeStopAtEnd: Boolean = Defaults.MARQUEE_STOP_AT_END,
    var marqueeInitialDelay: Int = Defaults.MARQUEE_INITIAL_DELAY,
    var marqueeRepeatUnlimited: Boolean = Defaults.MARQUEE_REPEAT_UNLIMITED,
    var enableGradientProgressStyle: Boolean = Defaults.ENABLE_GRADIENT_PROGRESS_STYLE,
) : AbstractStyle(), Parcelable {

    object Defaults {
        const val TEXT_SIZE = 0f
        val MARGINS = RectF()
        val PADDINGS = RectF()
        const val REPEAT_OUTPUT = false

        const val FADING_EDGE_ENABLED = false
        const val FADING_EDGE_LENGTH = 0

        const val ENABLE_CUSTOM_TEXT_COLOR = false
        val LIGHT_MODE_COLOR: TextColor? = null
        val DARK_MODE_COLOR: TextColor? = null

        val TYPE_FACE: String? = null
        const val TYPE_FACE_BOLD = false
        const val TYPE_FACE_ITALIC = false
        const val FONT_WEIGHT = -1

        const val MARQUEE_SPEED = 40f
        const val MARQUEE_GHOST_SPACING = 50f
        const val MARQUEE_LOOP_DELAY = 300
        const val MARQUEE_DELAY_ENABLE = false
        const val MARQUEE_REPEAT_COUNT = -1
        const val MARQUEE_STOP_AT_END = false
        const val MARQUEE_INITIAL_DELAY = 300
        const val MARQUEE_REPEAT_UNLIMITED = true
        const val ENABLE_GRADIENT_PROGRESS_STYLE = true
    }

    fun color(lightMode: Boolean) = if (lightMode) lightModeColor else darkModeColor

    override fun onLoad(preferences: SharedPreferences) {
        textSize = preferences.getFloat("lyric_style_text_size", Defaults.TEXT_SIZE)
        repeatOutput =
            preferences.getBoolean("lyric_style_text_repeat_output", Defaults.REPEAT_OUTPUT)
        margins = jsonx.safeDecode<RectF>(preferences.getString("lyric_style_text_margins", null))
        paddings = jsonx.safeDecode<RectF>(preferences.getString("lyric_style_text_paddings", null))

        enableCustomTextColor = preferences.getBoolean(
            "lyric_style_text_enable_custom_color",
            Defaults.ENABLE_CUSTOM_TEXT_COLOR
        )
        lightModeColor = jsonx.safeDecode<TextColor>(
            preferences.getString("lyric_style_text_color_light_mode", null),
            Defaults.LIGHT_MODE_COLOR
        )
        darkModeColor = jsonx.safeDecode<TextColor>(
            preferences.getString("lyric_style_text_color_dark_mode", null),
            Defaults.DARK_MODE_COLOR
        )

        fadingEdgeEnabled = preferences.getBoolean(
            "lyric_style_text_fading_edge_enabled",
            Defaults.FADING_EDGE_ENABLED
        )
        fadingEdgeLength =
            preferences.getInt("lyric_style_text_fading_edge_length", Defaults.FADING_EDGE_LENGTH)

        typeFace = preferences.getString("lyric_style_text_typeface", Defaults.TYPE_FACE)
        typeFaceBold =
            preferences.getBoolean("lyric_style_text_typeface_bold", Defaults.TYPE_FACE_BOLD)
        typeFaceItalic =
            preferences.getBoolean("lyric_style_text_typeface_italic", Defaults.TYPE_FACE_ITALIC)
        fontWeight = preferences.getInt("lyric_style_text_weight", Defaults.FONT_WEIGHT)

        marqueeSpeed =
            preferences.getFloat("lyric_style_text_marquee_speed", Defaults.MARQUEE_SPEED)
        marqueeGhostSpacing =
            preferences.getFloat("lyric_style_text_marquee_space", Defaults.MARQUEE_GHOST_SPACING)
        marqueeLoopDelay =
            preferences.getInt("lyric_style_text_marquee_loop_delay", Defaults.MARQUEE_LOOP_DELAY)
        marqueeInitialDelay = preferences.getInt(
            "lyric_style_text_marquee_initial_delay",
            Defaults.MARQUEE_INITIAL_DELAY
        )
        marqueeDelayEnable = preferences.getBoolean(
            "lyric_style_text_marquee_enable_delay",
            Defaults.MARQUEE_DELAY_ENABLE
        )
        marqueeRepeatCount = preferences.getInt(
            "lyric_style_text_marquee_repeat_count",
            Defaults.MARQUEE_REPEAT_COUNT
        )
        marqueeStopAtEnd = preferences.getBoolean(
            "lyric_style_text_marquee_stop_at_end",
            Defaults.MARQUEE_STOP_AT_END
        )
        marqueeRepeatUnlimited = preferences.getBoolean(
            "lyric_style_text_marquee_repeat_unlimited",
            Defaults.MARQUEE_REPEAT_UNLIMITED
        )
        enableGradientProgressStyle = preferences.getBoolean(
            "lyric_style_text_gradient_progress_style",
            Defaults.ENABLE_GRADIENT_PROGRESS_STYLE
        )
    }

    override fun onWrite(editor: SharedPreferences.Editor) {
        editor.putFloat("lyric_style_text_size", textSize)
        editor.putBoolean("lyric_style_text_repeat_output", repeatOutput)

        editor.putString("lyric_style_text_margins", margins.toJson())
        editor.putString("lyric_style_text_paddings", paddings.toJson())

        editor.putBoolean("lyric_style_text_enable_custom_color", enableCustomTextColor)
        editor.putString("lyric_style_text_color_light_mode", lightModeColor.toJson())
        editor.putString("lyric_style_text_color_dark_mode", darkModeColor.toJson())

        editor.putBoolean("lyric_style_text_fading_edge_enabled", fadingEdgeEnabled)
        editor.putInt("lyric_style_text_fading_edge_length", fadingEdgeLength)

        editor.putString("lyric_style_text_typeface", typeFace)
        editor.putBoolean("lyric_style_text_typeface_bold", typeFaceBold)
        editor.putBoolean("lyric_style_text_typeface_italic", typeFaceItalic)
        editor.putInt("lyric_style_text_weight", fontWeight)

        editor.putFloat("lyric_style_text_marquee_speed", marqueeSpeed)
        editor.putFloat("lyric_style_text_marquee_space", marqueeGhostSpacing)
        editor.putInt("lyric_style_text_marquee_loop_delay", marqueeLoopDelay)
        editor.putInt("lyric_style_text_marquee_initial_delay", marqueeInitialDelay)
        editor.putBoolean("lyric_style_text_marquee_enable_delay", marqueeDelayEnable)
        editor.putInt("lyric_style_text_marquee_repeat_count", marqueeRepeatCount)
        editor.putBoolean("lyric_style_text_marquee_stop_at_end", marqueeStopAtEnd)
        editor.putBoolean("lyric_style_text_marquee_repeat_unlimited", marqueeRepeatUnlimited)

        editor.putBoolean("lyric_style_text_gradient_progress_style", enableGradientProgressStyle)
    }
}