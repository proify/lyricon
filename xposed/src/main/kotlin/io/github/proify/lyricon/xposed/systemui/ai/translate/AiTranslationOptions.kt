/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.ai.translate

/**
 * 歌词翻译业务参数（功能级，仅 SystemUI 翻译流水线内部使用）。
 *
 * - [targetLanguage] 目标语言显示名；为空时回退系统显示语言；
 * - [stylePrompt] 用户自定义风格要求；为空时回退默认风格要求。
 *
 * 与连接配置 [io.github.proify.lyricon.lyric.ai.core.AiConfig] 分离：
 * 连接归连接，业务归业务，互不污染。
 */
data class AiTranslationOptions(
    val targetLanguage: String? = null,
    val stylePrompt: String? = null
)
