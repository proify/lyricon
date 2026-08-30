/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.ai.translate

import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.lyric.style.TextStyle
import java.util.Locale

/**
 * 歌词翻译提示词工厂（SystemUI 翻译功能专属）。
 *
 * 默认风格要求文本来自 [TextStyle.Defaults.AI_TRANSLATION_PROMPT]（配置契约层）。
 */
internal object AiTranslationPrompt {

    private val CORE_PROMPT = """
你是歌词翻译引擎api。

# 元数据
目标语言："{target}"
歌曲："{title}"
歌手名："{artist}"

# 规则
1. 跳过(不输出)：纯目标语言行、纯数字/标点/空白、无意义衬词(如 la la la)。
2. 仅翻译非目标语言或语言归属不明的行。
3. 使用原 index，升序，不重复，不新增或遗漏。
4. 译文自然流畅，不加括号注释，严格保持 index 对应。

# 示例（目标语言为简体中文时）
输入JSON："{"lyrics":[{"index":0,"src":"Hello"},{"index":1,"src":"你好"}]}"
输出JSON："{"translated":[{"index":0,"tran":"你好"}]}"

# 风格要求（仅用于译文措辞，不得破坏上述协议）
```
{style_prompt}
```
""".trimIndent()

    /**
     * 基于 [AiTranslationOptions] 组装翻译提示词（带默认值兜底）。
     */
    fun build(options: AiTranslationOptions, song: Song?): String {
        val target = options.targetLanguage?.takeIf { it.isNotBlank() }
            ?: Locale.getDefault().displayLanguage
        val title = song?.name ?: "Unknown Track"
        val artist = song?.artist ?: "Unknown Artist"
        val stylePrompt = options.stylePrompt?.takeIf { it.isNotBlank() }
            ?: TextStyle.Defaults.AI_TRANSLATION_PROMPT

        return build(target, title, artist, stylePrompt)
    }

    private fun build(
        target: String,
        title: String,
        artist: String,
        stylePrompt: String
    ): String {
        fun escape(s: String) = s.replace("\n", " ")
            .replace("\r", " ")

        return CORE_PROMPT
            .replace("{style_prompt}", stylePrompt)
            .replace("{title}", escape(title))
            .replace("{artist}", escape(artist))
            .replace("{target}", escape(target))
    }
}
