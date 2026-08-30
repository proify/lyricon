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
import io.github.proify.lyricon.lyric.ai.core.AiConfig
import io.github.proify.lyricon.lyric.style.BasicStyle.Companion.STATUS_COLOR_STRATEGY_COMPAT
import io.github.proify.lyricon.lyric.style.BasicStyle.Companion.STATUS_COLOR_STRATEGY_PRECISE
import io.github.proify.lyricon.lyric.style.TextStyle.Companion.KEY_AI_TRANSLATION_ENABLED
import io.github.proify.lyricon.lyric.style.TextStyle.Companion.KEY_AI_TRANSLATION_IGNORE_CHINESE
import io.github.proify.lyricon.lyric.style.TextStyle.Companion.KEY_AI_TRANSLATION_PROMPT
import io.github.proify.lyricon.lyric.style.TextStyle.Companion.KEY_AI_TRANSLATION_TARGET_LANGUAGE
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * 歌词基础样式配置类
 * 负责歌词显示位置、边距、隐藏规则以及简繁体转换等基础逻辑
 */
@Serializable
@Parcelize
data class BasicStyle(
    var anchor: String = Defaults.ANCHOR,
    var insertionOrder: Int = Defaults.INSERTION_ORDER,

    /**
     * 状态栏文字颜色同步策略 ID(见 [STATUS_COLOR_STRATEGY_PRECISE] /
     * [STATUS_COLOR_STRATEGY_COMPAT]),由 [StatusBarColorMonitor] 注册的策略动态执行,
     * 未来可扩展新策略
     */
    var statusColorStrategy: Int = Defaults.STATUS_COLOR_STRATEGY,

    private var width: Float = Defaults.WIDTH,
    private var widthInLand: Float = Defaults.WIDTH_LAND,

    private var widthInColorOSCapsuleMode: Float = Defaults.WIDTH_IN_COLOROS_CAPSULE_MODE,
    private var widthInColorOSCapsuleModeLand: Float = Defaults.WIDTH_IN_COLOROS_CAPSULE_MODE_LAND,

    var margins: RectF = Defaults.MARGINS,
    var paddings: RectF = Defaults.PADDINGS,
    var visibilityRules: List<VisibilityRule> = Defaults.VISIBILITY_RULES,
    var hideOnLockScreen: Boolean = Defaults.HIDE_ON_LOCK_SCREEN,
    var noLyricHideTimeout: Int = Defaults.NO_LYRIC_HIDE_TIMEOUT,
    var noUpdateHideTimeout: Int = Defaults.NO_UPDATE_HIDE_TIMEOUT,
    var keywordHideTimeout: Int = Defaults.KEYWORD_HIDE_TIMEOUT,
    var keywordHideMatches: List<String> = Defaults.KEYWORD_HIDE_MATCH,
    var blockedWordsRegexString: String = Defaults.BLOCKED_WORDS_REGEX,
    var chineseConversionMode: Int = Defaults.CHINESE_CONVERSION_MODE,

    var isAiTranslationEnable: Boolean = false,
    var aiConfigs: AiConfig? = null,
    var aiTranslationTargetLanguage: String = TextStyle.Defaults.AI_TRANSLATION_TARGET_LANGUAGE_DISPLAY_NAME,
    var aiTranslationPrompt: String = TextStyle.Defaults.AI_TRANSLATION_PROMPT,
    var isAiTranslationAutoIgnoreChinese: Boolean = false,

    ) : AbstractStyle(), Parcelable {

    fun getAutoWidth(isLand: Boolean, isOplusCapsuleShowing: Boolean): Float {
        if (isOplusCapsuleShowing) return getWidthInColorOSCapsuleMode(isLand)
        return getWidth(isLand)
    }

    fun getWidth(isLand: Boolean): Float {
        return if (isLand) widthInLand else width
    }

    fun getWidthInColorOSCapsuleMode(isLand: Boolean): Float {
        return if (isLand) widthInColorOSCapsuleModeLand else widthInColorOSCapsuleMode
    }

    /** 缓存的屏蔽词正则表达式对象 */
    @IgnoredOnParcel
    @Transient
    var blockedWordsRegex: Regex? = null
        get() = if (field == null) {
            field = try {
                if (blockedWordsRegexString.isNotBlank())
                    Regex(blockedWordsRegexString) else null
            } catch (_: Exception) {
                null
            }
            field
        } else {
            field
        }

    /** 缓存的关键语隐藏正则表达式列表 */
    @IgnoredOnParcel
    @Transient
    var keywordsHidePattern: List<Regex>? = null
        get() = if (field == null) {
            field = keywordHideMatches.mapNotNull {
                runCatching { Regex(it) }.getOrNull()
            }
            field
        } else {
            field
        }

    override fun onLoad(preferences: SharedPreferences) {
        anchor =
            preferences.getString("lyric_style_base_anchor", Defaults.ANCHOR) ?: Defaults.ANCHOR
        insertionOrder =
            preferences.getInt("lyric_style_base_insertion_order", Defaults.INSERTION_ORDER)
        statusColorStrategy = preferences.getInt(
            "lyric_style_base_status_color_strategy",
            Defaults.STATUS_COLOR_STRATEGY
        )

        width = preferences.getFloat("lyric_style_base_width", Defaults.WIDTH)
        widthInLand =
            preferences.getFloat("lyric_style_base_width_in_landscape", Defaults.WIDTH_LAND)
        widthInColorOSCapsuleMode = preferences.getFloat(
            "lyric_style_base_width_in_coloros_capsule_mode",
            Defaults.WIDTH_IN_COLOROS_CAPSULE_MODE
        )
        widthInColorOSCapsuleModeLand = preferences.getFloat(
            "lyric_style_base_width_in_coloros_capsule_mode_in_landscape",
            Defaults.WIDTH_IN_COLOROS_CAPSULE_MODE_LAND
        )

        margins = json.safeDecode<RectF>(
            preferences.getString("lyric_style_base_margins", null),
            Defaults.MARGINS
        )
        paddings = json.safeDecode<RectF>(
            preferences.getString("lyric_style_base_paddings", null),
            Defaults.PADDINGS
        )
        visibilityRules = json.safeDecode<MutableList<VisibilityRule>>(
            preferences.getString(
                "lyric_style_base_visibility_rules",
                "[]"
            ), Defaults.VISIBILITY_RULES.toMutableList()
        )

        hideOnLockScreen = preferences.getBoolean(
            "lyric_style_base_hide_on_lock_screen",
            Defaults.HIDE_ON_LOCK_SCREEN
        )
        noLyricHideTimeout = preferences.getInt(
            "lyric_style_base_no_lyric_hide_timeout",
            Defaults.NO_LYRIC_HIDE_TIMEOUT
        )
        noUpdateHideTimeout = preferences.getInt(
            "lyric_style_base_no_update_hide_timeout",
            Defaults.NO_UPDATE_HIDE_TIMEOUT
        )
        keywordHideTimeout = preferences.getInt(
            "lyric_style_base_keyword_hide_timeout",
            Defaults.KEYWORD_HIDE_TIMEOUT
        )

        preferences.getString("lyric_style_base_timeout_hide_keywords", null)?.let {
            keywordHideMatches = json.safeDecode<List<String>>(it, emptyList())
            keywordsHidePattern = null
        }

        blockedWordsRegexString = preferences.getString(
            "lyric_style_base_blocked_words_regex",
            Defaults.BLOCKED_WORDS_REGEX
        ) ?: Defaults.BLOCKED_WORDS_REGEX
        blockedWordsRegex = null

        chineseConversionMode = preferences.getInt(
            "lyric_style_base_chinese_conversion_mode",
            Defaults.CHINESE_CONVERSION_MODE
        )

        isAiTranslationEnable =
            preferences.getBoolean(
                KEY_AI_TRANSLATION_ENABLED,
                TextStyle.Defaults.AI_TRANSLATION_ENABLED
            )
        aiConfigs = AiConfig.fromPreferences(preferences)
        aiTranslationTargetLanguage =
            preferences.getString(
                KEY_AI_TRANSLATION_TARGET_LANGUAGE,
                TextStyle.Defaults.AI_TRANSLATION_TARGET_LANGUAGE_DISPLAY_NAME
            ) ?: TextStyle.Defaults.AI_TRANSLATION_TARGET_LANGUAGE_DISPLAY_NAME
        aiTranslationPrompt =
            preferences.getString(
                KEY_AI_TRANSLATION_PROMPT,
                TextStyle.Defaults.AI_TRANSLATION_PROMPT
            ) ?: TextStyle.Defaults.AI_TRANSLATION_PROMPT
        isAiTranslationAutoIgnoreChinese =
            preferences.getBoolean(
                KEY_AI_TRANSLATION_IGNORE_CHINESE,
                TextStyle.Defaults.AI_TRANSLATION_IGNORE_CHINESE
            )
    }

    override fun onWrite(editor: SharedPreferences.Editor) {
        editor.putString("lyric_style_base_anchor", anchor)
        editor.putInt("lyric_style_base_insertion_order", insertionOrder)
        editor.putInt(
            "lyric_style_base_status_color_strategy",
            statusColorStrategy
        )

        editor.putFloat("lyric_style_base_width", width)
        editor.putFloat("lyric_style_base_width_in_landscape", widthInLand)

        editor.putFloat("lyric_style_base_width_in_coloros_capsule_mode", widthInColorOSCapsuleMode)
        editor.putFloat(
            "lyric_style_base_width_in_coloros_capsule_mode_in_landscape",
            widthInColorOSCapsuleModeLand
        )

        editor.putString("lyric_style_base_margins", margins.toJson())
        editor.putString("lyric_style_base_paddings", paddings.toJson())
        editor.putString("lyric_style_base_visibility_rules", visibilityRules.toJson())
        editor.putBoolean("lyric_style_base_hide_on_lock_screen", hideOnLockScreen)
        editor.putInt("lyric_style_base_no_lyric_hide_timeout", noLyricHideTimeout)
        editor.putInt("lyric_style_base_no_update_hide_timeout", noUpdateHideTimeout)
        editor.putInt("lyric_style_base_keyword_hide_timeout", keywordHideTimeout)
        editor.putString("lyric_style_base_timeout_hide_keywords", keywordHideMatches.toJson())
        editor.putString("lyric_style_base_blocked_words_regex", blockedWordsRegexString)

        editor.putInt("lyric_style_base_chinese_conversion_mode", chineseConversionMode)

        editor.putBoolean(KEY_AI_TRANSLATION_ENABLED, isAiTranslationEnable)
        aiConfigs?.let { AiConfig.writeTo(editor, it) }
        editor.putString(KEY_AI_TRANSLATION_TARGET_LANGUAGE, aiTranslationTargetLanguage)
        editor.putString(KEY_AI_TRANSLATION_PROMPT, aiTranslationPrompt)
        editor.putBoolean(KEY_AI_TRANSLATION_IGNORE_CHINESE, isAiTranslationAutoIgnoreChinese)
    }

    object Defaults {
        const val ANCHOR: String = "clock"
        const val INSERTION_ORDER: Int = INSERTION_ORDER_BEFORE
        const val STATUS_COLOR_STRATEGY: Int = STATUS_COLOR_STRATEGY_PRECISE
        const val WIDTH: Float = 100f
        const val WIDTH_LAND: Float = 200f

        const val WIDTH_IN_COLOROS_CAPSULE_MODE: Float = 70f
        const val WIDTH_IN_COLOROS_CAPSULE_MODE_LAND: Float = 70f

        val MARGINS: RectF = RectF()
        val PADDINGS: RectF = RectF()
        val VISIBILITY_RULES: List<VisibilityRule> = emptyList()
        const val HIDE_ON_LOCK_SCREEN: Boolean = true
        const val NO_LYRIC_HIDE_TIMEOUT: Int = 0
        const val NO_UPDATE_HIDE_TIMEOUT: Int = 0
        const val KEYWORD_HIDE_TIMEOUT: Int = 0
        val KEYWORD_HIDE_MATCH: List<String> = listOf()
        const val BLOCKED_WORDS_REGEX: String = ""
        const val CHINESE_CONVERSION_MODE: Int = CHINESE_CONVERSION_OFF
    }

    companion object {
        const val INSERTION_ORDER_BEFORE: Int = 0
        const val INSERTION_ORDER_AFTER: Int = 1

        /** 状态栏文字颜色同步策略:精准模式(按时钟类动态 hook,默认) */
        const val STATUS_COLOR_STRATEGY_PRECISE: Int = 0

        /** 状态栏文字颜色同步策略:兼容模式(全局 TextView#setTextColor + 聚合) */
        const val STATUS_COLOR_STRATEGY_COMPAT: Int = 1

        /** 中文转换模式：关闭 */
        const val CHINESE_CONVERSION_OFF = 0

        /** 中文转换模式：简体中文 */
        const val CHINESE_CONVERSION_SIMPLIFIED = 1

        /** 中文转换模式：繁体中文 */
        const val CHINESE_CONVERSION_TRADITIONAL = 2
    }
}