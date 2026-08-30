/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.ai.translate

import android.util.Log
import io.github.proify.android.extensions.json
import io.github.proify.lyricon.lyric.ai.core.AiChatClient
import io.github.proify.lyricon.lyric.ai.core.AiConfig
import io.github.proify.lyricon.lyric.model.Song

/**
 * 歌词翻译请求组装器。
 *
 * 职责：筛选需要翻译的歌词行 → 组装 JSON 请求体 → 调用共享的 [AiChatClient] →
 * 交给 [AiTranslationResponseParser] 解析。网络与鉴权细节在 [AiChatClient]。
 */
internal object AiTranslationRequester {
    private const val TAG = "LyriconAiTranslator"

    suspend fun request(
        configs: AiConfig,
        options: AiTranslationOptions,
        song: Song? = null,
        texts: List<String>
    ): List<TranslationItem>? {
        if (configs.apiKey.isNullOrBlank()) {
            Log.e(TAG, "Request aborted: API Key is null or blank.")
            return null
        }

        val requestItems = texts.mapIndexedNotNull { index, text ->
            text.trim().takeIf(::shouldRequestTranslation)?.let {
                TranslationRequestItem(index = index, text = it)
            }
        }
        if (requestItems.isEmpty()) {
            Log.d(TAG, "Request skipped: no translatable lyric lines.")
            return emptyList()
        }

        val payload = json.encodeToString(TranslationRequest(requestItems))
        Log.d(TAG, "Requesting translations, payload: $payload")

        val requestIndices = requestItems.map { it.index }.toSet()
        val content = AiChatClient.complete(
            configs = configs,
            systemPrompt = AiTranslationPrompt.build(options, song),
            userPrompt = payload,
            jsonObject = true
        ) ?: run {
            Log.e(TAG, "OpenAI Chat request returned null.")
            return null
        }

        Log.d(TAG, "Parsing JSON response: $content")
        return AiTranslationResponseParser.parse(content, requestIndices)
    }

    /** 只请求包含字母（语言可识别）的行；纯数字/标点/空白行不消耗 API 额度。 */
    private fun shouldRequestTranslation(text: String): Boolean {
        if (text.isBlank()) return false
        return text.any { it.isLetter() }
    }
}
