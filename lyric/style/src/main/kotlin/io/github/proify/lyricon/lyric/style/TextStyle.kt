/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.lyric.style

import android.content.SharedPreferences
import android.os.Parcelable
import io.github.proify.android.extensions.json
import io.github.proify.android.extensions.safeDecode
import io.github.proify.android.extensions.toJson
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
@Parcelize
data class TextStyle(
    var textSize: Float = Defaults.TEXT_SIZE,
    var margins: RectF = Defaults.MARGINS,
    var paddings: RectF = Defaults.PADDINGS,
    var repeatOutput: Boolean = Defaults.REPEAT_OUTPUT,

    var fadingEdgeLength: Int = Defaults.FADING_EDGE_LENGTH,

    var enableCustomTextColor: Boolean = Defaults.ENABLE_CUSTOM_TEXT_COLOR,
    var enableExtractCoverTextColor: Boolean = Defaults.ENABLE_EXTRACT_COVER_TEXT_COLOR,
    var enableExtractCoverTextGradient: Boolean = Defaults.ENABLE_EXTRACT_COVER_TEXT_GRADIENT,
    var lightModeRainbowColor: RainbowTextColor? = Defaults.LIGHT_MODE_RAINBOW_COLOR,
    var darkModeRainbowColor: RainbowTextColor? = Defaults.DARK_MODE_RAINBOW_COLOR,

    var typeFace: String? = Defaults.TYPE_FACE,
    var typeFaceBold: Boolean = Defaults.TYPE_FACE_BOLD,
    var typeFaceItalic: Boolean = Defaults.TYPE_FACE_ITALIC,
    var fontWeight: Int = Defaults.FONT_WEIGHT,

    var marqueeSpeed: Float = Defaults.MARQUEE_SPEED,
    var marqueeGhostSpacing: Float = Defaults.MARQUEE_GHOST_SPACING,
    var marqueeLoopDelay: Int = Defaults.MARQUEE_LOOP_DELAY,
    var marqueeRepeatCount: Int = Defaults.MARQUEE_REPEAT_COUNT,
    var marqueeStopAtEnd: Boolean = Defaults.MARQUEE_STOP_AT_END,
    var marqueeInitialDelay: Int = Defaults.MARQUEE_INITIAL_DELAY,
    var marqueeRepeatUnlimited: Boolean = Defaults.MARQUEE_REPEAT_UNLIMITED,
    var gradientProgressStyle: Boolean = Defaults.ENABLE_GRADIENT_PROGRESS_STYLE,

    var relativeProgress: Boolean = Defaults.RELATIVE_PROGRESS,
    var relativeProgressHighlight: Boolean = Defaults.RELATIVE_PROGRESS_HIGHLIGHT,
    var wordMotionEnabled: Boolean = Defaults.WORD_MOTION_ENABLED,
    var wordMotionCjkLiftFactor: Float = Defaults.WORD_MOTION_CJK_LIFT_FACTOR,
    var wordMotionCjkWaveFactor: Float = Defaults.WORD_MOTION_CJK_WAVE_FACTOR,
    var wordMotionLatinLiftFactor: Float = Defaults.WORD_MOTION_LATIN_LIFT_FACTOR,
    var wordMotionLatinWaveFactor: Float = Defaults.WORD_MOTION_LATIN_WAVE_FACTOR,
    var scaleInMultiLine: Float = Defaults.TEXT_SIZE_RATIO_IN_MULTI_LINE,

    var transitionConfig: String? = Defaults.TRANSITION_CONFIG,
    var placeholderFormat: String? = Defaults.PLACEHOLDER_FORMAT,

    var isDisableTranslation: Boolean = false,
    var isTranslationOnly: Boolean = false,

    var enableEnterAnim: Boolean = false
) : Parcelable, AbstractStyle() {

    companion object {
        const val TRANSITION_CONFIG_FAST: String = "fast"
        const val TRANSITION_CONFIG_SMOOTH: String = "smooth"
        const val TRANSITION_CONFIG_SLOW: String = "slow"
        const val TRANSITION_CONFIG_NONE = "none"

        // ===== AI 连接配置 keys 已迁移至 :lyric:ai（AiConfig） =====
        // ===== AI 歌词翻译（功能级配置）=====
        const val KEY_AI_TRANSLATION_ENABLED = "ai_translation_enabled"
        const val KEY_AI_TRANSLATION_TARGET_LANGUAGE = "ai_translation_target_language"
        const val KEY_AI_TRANSLATION_TARGET_LANGUAGE_CODE = "ai_translation_target_language_code"
        const val KEY_AI_TRANSLATION_PROMPT = "ai_translation_prompt"
        const val KEY_AI_TRANSLATION_IGNORE_CHINESE = "ai_translation_ignore_chinese"

        const val KEY_TEXT_TRANSLATION_ONLY = "lyric_style_text_translation_only"
        const val KEY_TEXT_TRANSLATION_DISABLE = "lyric_style_text_translation_disable"
        const val KEY_WORD_MOTION_ENABLED = "lyric_style_text_word_motion_enabled"
        const val KEY_WORD_MOTION_CJK_LIFT_FACTOR = "lyric_style_text_word_motion_cjk_lift_factor"
        const val KEY_WORD_MOTION_CJK_WAVE_FACTOR = "lyric_style_text_word_motion_cjk_wave_factor"
        const val KEY_WORD_MOTION_LATIN_LIFT_FACTOR =
            "lyric_style_text_word_motion_latin_lift_factor"
        const val KEY_WORD_MOTION_LATIN_WAVE_FACTOR =
            "lyric_style_text_word_motion_latin_wave_factor"
        const val KEY_ENABLED_ENTER_ANIM = "lyric_style_text_enable_enter_anim"
    }

    object PlaceholderFormat {
        const val NAME: String = "NameOnly"
        const val NAME_ARTIST: String = "NameAndArtist"
        const val NONE: String = "None"
    }

    object Defaults {
        const val TRANSLATION_ONLY: Boolean = false
        const val TRANSLATION_DISABLE: Boolean = false

        // ===== AI 歌词翻译（功能级默认值）=====
        const val AI_TRANSLATION_ENABLED: Boolean = false
        val AI_TRANSLATION_TARGET_LANGUAGE_DISPLAY_NAME: String
            get() {
                val locale = Locale.getDefault()
                val language = locale.getDisplayLanguage(locale)
                val script = locale.getDisplayScript(locale)

                return when {
                    !script.isNullOrBlank() -> script
                    else -> language
                }
            }

        const val AI_TRANSLATION_PROMPT: String =
            "语境迁移：贴合背景、身份和情感。\n" +
                    "隐喻转化：替换为本地惯用比喻，舍字面保意境。\n" +
                    "曲风适配：民谣克制留白，摇滚直接锋利，说唱押韵顺 flow。\n" +
                    "习惯优先：完全用目标语言习惯用语和自然语序，杜绝翻译腔。"
        const val AI_TRANSLATION_IGNORE_CHINESE = false

        const val PLACEHOLDER_FORMAT: String = PlaceholderFormat.NAME
        const val TRANSITION_CONFIG: String = TRANSITION_CONFIG_SMOOTH

        const val TEXT_SIZE_RATIO_IN_MULTI_LINE: Float = 0.86f
        const val RELATIVE_PROGRESS: Boolean = true
        const val RELATIVE_PROGRESS_HIGHLIGHT: Boolean = false
        const val WORD_MOTION_ENABLED: Boolean = false
        const val WORD_MOTION_CJK_LIFT_FACTOR: Float = 0.055f
        const val WORD_MOTION_CJK_WAVE_FACTOR: Float = 2.8f
        const val WORD_MOTION_LATIN_LIFT_FACTOR: Float = 0.065f
        const val WORD_MOTION_LATIN_WAVE_FACTOR: Float = 3.6f

        const val TEXT_SIZE: Float = 0f
        val MARGINS: RectF = RectF()
        val PADDINGS: RectF = RectF()
        const val REPEAT_OUTPUT: Boolean = false

        const val FADING_EDGE_LENGTH: Int = 14

        const val ENABLE_CUSTOM_TEXT_COLOR: Boolean = false
        const val ENABLE_EXTRACT_COVER_TEXT_COLOR: Boolean = false
        const val ENABLE_EXTRACT_COVER_TEXT_GRADIENT: Boolean = false

        val LIGHT_MODE_RAINBOW_COLOR: RainbowTextColor? = null
        val DARK_MODE_RAINBOW_COLOR: RainbowTextColor? = null

        val TYPE_FACE: String? = null
        const val TYPE_FACE_BOLD: Boolean = false
        const val TYPE_FACE_ITALIC: Boolean = false
        const val FONT_WEIGHT: Int = -1

        const val MARQUEE_SPEED: Float = 35f
        const val MARQUEE_GHOST_SPACING: Float = 50f
        const val MARQUEE_LOOP_DELAY: Int = 0

        const val MARQUEE_REPEAT_COUNT: Int = -1
        const val MARQUEE_STOP_AT_END: Boolean = false
        const val MARQUEE_INITIAL_DELAY: Int = 300
        const val MARQUEE_REPEAT_UNLIMITED: Boolean = true
        const val ENABLE_GRADIENT_PROGRESS_STYLE: Boolean = true
    }

    fun color(lightMode: Boolean): RainbowTextColor? =
        if (lightMode) lightModeRainbowColor else darkModeRainbowColor

    override fun onLoad(preferences: SharedPreferences) {
        textSize = preferences.getFloat("lyric_style_text_size", Defaults.TEXT_SIZE)
        repeatOutput =
            preferences.getBoolean("lyric_style_text_repeat_output", Defaults.REPEAT_OUTPUT)
        margins = json.safeDecode<RectF>(preferences.getString("lyric_style_text_margins", null))
        paddings = json.safeDecode<RectF>(preferences.getString("lyric_style_text_paddings", null))

        enableCustomTextColor = preferences.getBoolean(
            "lyric_style_text_enable_custom_color",
            Defaults.ENABLE_CUSTOM_TEXT_COLOR
        )
        enableExtractCoverTextColor = preferences.getBoolean(
            "lyric_style_text_extract_cover_color",
            Defaults.ENABLE_EXTRACT_COVER_TEXT_COLOR
        )
        enableExtractCoverTextGradient = preferences.getBoolean(
            "lyric_style_text_extract_cover_gradient",
            Defaults.ENABLE_EXTRACT_COVER_TEXT_GRADIENT
        )

        if (!enableExtractCoverTextColor) {
            enableExtractCoverTextGradient = false
        }
        lightModeRainbowColor = json.safeDecode<RainbowTextColor>(
            preferences.getString("lyric_style_text_rainbow_color_light_mode", null),
            Defaults.LIGHT_MODE_RAINBOW_COLOR
        )
        darkModeRainbowColor = json.safeDecode<RainbowTextColor>(
            preferences.getString("lyric_style_text_rainbow_color_dark_mode", null),
            Defaults.DARK_MODE_RAINBOW_COLOR
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
        gradientProgressStyle = preferences.getBoolean(
            "lyric_style_text_gradient_progress_style",
            Defaults.ENABLE_GRADIENT_PROGRESS_STYLE
        )

        relativeProgress = preferences.getBoolean(
            "lyric_style_text_relative_progress",
            Defaults.RELATIVE_PROGRESS
        )
        relativeProgressHighlight = preferences.getBoolean(
            "lyric_style_text_relative_progress_highlight",
            Defaults.RELATIVE_PROGRESS_HIGHLIGHT
        )
        wordMotionEnabled = preferences.getBoolean(
            KEY_WORD_MOTION_ENABLED,
            Defaults.WORD_MOTION_ENABLED
        )
        wordMotionCjkLiftFactor = preferences.getFloat(
            KEY_WORD_MOTION_CJK_LIFT_FACTOR,
            Defaults.WORD_MOTION_CJK_LIFT_FACTOR
        )
        wordMotionCjkWaveFactor = preferences.getFloat(
            KEY_WORD_MOTION_CJK_WAVE_FACTOR,
            Defaults.WORD_MOTION_CJK_WAVE_FACTOR
        )
        wordMotionLatinLiftFactor = preferences.getFloat(
            KEY_WORD_MOTION_LATIN_LIFT_FACTOR,
            Defaults.WORD_MOTION_LATIN_LIFT_FACTOR
        )
        wordMotionLatinWaveFactor = preferences.getFloat(
            KEY_WORD_MOTION_LATIN_WAVE_FACTOR,
            Defaults.WORD_MOTION_LATIN_WAVE_FACTOR
        )
        scaleInMultiLine = preferences.getFloat(
            "lyric_style_text_size_ratio_in_multi_line_mode",
            Defaults.TEXT_SIZE_RATIO_IN_MULTI_LINE
        )

        transitionConfig = preferences.getString(
            "lyric_style_text_transition_config",
            Defaults.TRANSITION_CONFIG
        )
        placeholderFormat = preferences.getString(
            "lyric_style_text_placeholder_format",
            Defaults.PLACEHOLDER_FORMAT
        )

        isDisableTranslation = preferences.getBoolean(
            KEY_TEXT_TRANSLATION_DISABLE,
            Defaults.TRANSLATION_DISABLE
        )
        isTranslationOnly = preferences.getBoolean(
            KEY_TEXT_TRANSLATION_ONLY,
            Defaults.TRANSLATION_ONLY
        )


        enableEnterAnim = preferences.getBoolean(KEY_ENABLED_ENTER_ANIM, false)
    }

    override fun onWrite(editor: SharedPreferences.Editor) {
        editor.putFloat("lyric_style_text_size", textSize)
        editor.putBoolean("lyric_style_text_repeat_output", repeatOutput)

        editor.putString("lyric_style_text_margins", margins.toJson())
        editor.putString("lyric_style_text_paddings", paddings.toJson())

        editor.putBoolean("lyric_style_text_enable_custom_color", enableCustomTextColor)
        editor.putBoolean("lyric_style_text_extract_cover_color", enableExtractCoverTextColor)
        editor.putBoolean(
            "lyric_style_text_extract_cover_gradient",
            enableExtractCoverTextGradient
        )
        editor.putString(
            "lyric_style_text_rainbow_color_light_mode",
            lightModeRainbowColor.toJson()
        )
        editor.putString("lyric_style_text_rainbow_color_dark_mode", darkModeRainbowColor.toJson())

        editor.putInt("lyric_style_text_fading_edge_length", fadingEdgeLength)

        editor.putString("lyric_style_text_typeface", typeFace)
        editor.putBoolean("lyric_style_text_typeface_bold", typeFaceBold)
        editor.putBoolean("lyric_style_text_typeface_italic", typeFaceItalic)
        editor.putInt("lyric_style_text_weight", fontWeight)

        editor.putFloat("lyric_style_text_marquee_speed", marqueeSpeed)
        editor.putFloat("lyric_style_text_marquee_space", marqueeGhostSpacing)
        editor.putInt("lyric_style_text_marquee_loop_delay", marqueeLoopDelay)
        editor.putInt("lyric_style_text_marquee_initial_delay", marqueeInitialDelay)
        editor.putInt("lyric_style_text_marquee_repeat_count", marqueeRepeatCount)
        editor.putBoolean("lyric_style_text_marquee_stop_at_end", marqueeStopAtEnd)
        editor.putBoolean("lyric_style_text_marquee_repeat_unlimited", marqueeRepeatUnlimited)

        editor.putBoolean("lyric_style_text_gradient_progress_style", gradientProgressStyle)

        editor.putBoolean("lyric_style_text_relative_progress", relativeProgress)
        editor.putBoolean(
            "lyric_style_text_relative_progress_highlight",
            relativeProgressHighlight
        )
        editor.putBoolean(KEY_WORD_MOTION_ENABLED, wordMotionEnabled)
        editor.putFloat(KEY_WORD_MOTION_CJK_LIFT_FACTOR, wordMotionCjkLiftFactor)
        editor.putFloat(KEY_WORD_MOTION_CJK_WAVE_FACTOR, wordMotionCjkWaveFactor)
        editor.putFloat(KEY_WORD_MOTION_LATIN_LIFT_FACTOR, wordMotionLatinLiftFactor)
        editor.putFloat(KEY_WORD_MOTION_LATIN_WAVE_FACTOR, wordMotionLatinWaveFactor)
        editor.putFloat(
            "lyric_style_text_size_ratio_in_multi_line_mode",
            scaleInMultiLine
        )

        editor.putString(
            "lyric_style_text_transition_config",
            transitionConfig
        )
        editor.putString("lyric_style_text_placeholder_format", placeholderFormat)

        editor.putBoolean(KEY_TEXT_TRANSLATION_DISABLE, isDisableTranslation)
        editor.putBoolean(KEY_TEXT_TRANSLATION_ONLY, isTranslationOnly)

        editor.putBoolean(KEY_ENABLED_ENTER_ANIM, enableEnterAnim)
    }

}
