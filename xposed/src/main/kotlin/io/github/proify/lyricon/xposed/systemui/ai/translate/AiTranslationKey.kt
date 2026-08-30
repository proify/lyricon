/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.ai.translate

import io.github.proify.android.extensions.md5
import io.github.proify.lyricon.lyric.ai.core.AiConfig
import io.github.proify.lyricon.lyric.model.Song

/**
 * 整首歌级翻译缓存 key 生成器。
 *
 * key 必须覆盖影响译文的所有输入：服务商 / Base URL / 模型 / 目标语言 /
 * 风格要求 / 歌曲元数据 / 逐行原文——任何一项变更都会生成新的 key，
 * 避免命中其他配置下的陈旧译文。
 */
internal object AiTranslationKey {

    fun calculate(
        configs: AiConfig,
        options: AiTranslationOptions,
        song: Song,
        lines: List<String>
    ): String {
        return buildString {
            append("provider=").appendLine(configs.provider.orEmpty())
            append("baseUrl=").appendLine(configs.baseUrl.orEmpty())
            append("model=").appendLine(configs.model.orEmpty())
            append("target=").appendLine(options.targetLanguage.orEmpty())
            append("style=").appendLine(options.stylePrompt.orEmpty())
            append("title=").appendLine(song.name.orEmpty())
            append("artist=").appendLine(song.artist.orEmpty())
            lines.forEachIndexed { index, line ->
                append(index).append(':').appendLine(line)
            }
        }.md5()
    }
}
