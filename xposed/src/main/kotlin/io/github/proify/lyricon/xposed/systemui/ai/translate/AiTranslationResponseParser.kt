/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.ai.translate

import android.util.Log
import io.github.proify.android.extensions.json

/**
 * LLM 响应清洗与兼容解析：
 * - 兼容 LLM 包裹 ```json 代码块等脏输出；
 * - 只接受请求索引内的有效译文（去重、去空白、拒绝空值）；
 * - 结果按 index 升序排列（提示词协议约定"使用原 index，升序"）。
 */
internal object AiTranslationResponseParser {
    private const val TAG = "LyriconAiTranslator"

    fun parse(content: String, requestIndices: Set<Int>): List<TranslationItem> {
        val items = decodeTranslationItems(content)
        val validItems = normalizeTranslationItems(items, requestIndices)
        Log.d(TAG, "API call successful, parsed ${items.size} items, accepted ${validItems.size}.")
        return validItems
    }

    private fun decodeTranslationItems(content: String): List<TranslationItem> {
        return try {
            json.decodeFromString<TranslationResponse>(content).translated
        } catch (_: Exception) {
            json.decodeFromString<TranslationResponse>(cleanOpenAIResponse(content)).translated
        }
    }

    private fun cleanOpenAIResponse(rawResponse: String): String {
        return rawResponse
            .replace(Regex("^```json\\s*\\n?"), "")  // 移除开头的 ```json
            .replace(Regex("^```\\s*\\n?"), "")       // 移除开头的 ```
            .replace(Regex("\\n?```\\s*$"), "")       // 移除结尾的 ```
            .trim()
    }

    private fun normalizeTranslationItems(
        items: List<TranslationItem>,
        requestIndices: Set<Int>
    ): List<TranslationItem> {
        val accepted = LinkedHashMap<Int, TranslationItem>()
        items.forEach { item ->
            val translation = item.translation.trim()
            if (item.index in requestIndices && translation.isNotBlank() && item.index !in accepted) {
                accepted[item.index] = item.copy(translation = translation)
            }
        }
        return accepted.values.sortedBy { it.index }
    }
}
