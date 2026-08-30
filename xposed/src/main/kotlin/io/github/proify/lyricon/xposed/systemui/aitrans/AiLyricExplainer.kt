/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.aitrans

import android.util.Log
import io.github.proify.android.extensions.json
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.lyric.style.AiTranslationConfigs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.EOFException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * AI 歌词解释器 (AI Lyric Explainer)
 *
 * 独立于整首歌级联翻译流水线之外的一种轻量 AI 交互。
 * 由"状态栏歌词控制窗口"触发，将当前正在播放的歌词发送给
 * OpenAI 兼容服务，请求返回对该段歌词的语义解读 / 赏析说明。
 *
 * 与 [AITranslator] 的区别：
 * - [AITranslator] 面向整首歌的批量翻译，走带缓存与调度队列的流水线；
 * - [AiLyricExplainer] 面向单次"解释"交互，直接请求并返回纯文本解释。
 *
 * 配置复用 [AiTranslationConfigs]（baseUrl / apiKey / model / temperature 等），
 * 不额外引入新配置项，方便用户开箱即用。
 *
 * @author Tomakino
 * @since 2026
 */
object AiLyricExplainer {
    private const val TAG = "AiLyricExplainer"

    /**
     * 请求 AI 解释当前歌词。
     *
     * @param configs AI 配置，需满足 [AiTranslationConfigs.isUsable]
     * @param song 当前歌曲信息（用于提供歌名 / 歌手元数据），可为 null
     * @param lyrics 需要解释的歌词文本（通常是当前行或上下文数行）
     * @return 解释结果文本；配置不可用或请求失败时返回 null
     */
    suspend fun explain(
        configs: AiTranslationConfigs,
        song: Song?,
        lyrics: String
    ): String? {
        if (!configs.isUsable) {
            Log.w(TAG, "Explain skipped: Configs not usable (missing API Key or disabled).")
            return null
        }
        if (lyrics.isBlank()) {
            Log.w(TAG, "Explain skipped: Lyrics is blank.")
            return null
        }

        return withContext(Dispatchers.IO) {
            try {
                val result = request(configs, song, lyrics)
                result?.takeIf { it.isNotBlank() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI lyric explanation failed.", e)
                null
            }
        }
    }

    /**
     * 向 OpenAI 兼容接口发送一次"解释歌词"请求。
     */
    private suspend fun request(
        configs: AiTranslationConfigs,
        song: Song?,
        lyrics: String
    ): String? = withContext(Dispatchers.IO) {
        if (configs.apiKey.isNullOrBlank()) {
            Log.e(TAG, "Request aborted: API Key is null or blank.")
            return@withContext null
        }

        val baseUrl = configs.baseUrl?.removeSuffix("/") ?: "https://api.openai.com/v1"
        val apiUrl =
            if (baseUrl.endsWith("/chat/completions")) baseUrl else "$baseUrl/chat/completions"

        val chatRequest = OpenAiChatRequest(
            model = configs.model.orEmpty(),
            messages = listOf(
                ChatMessage("system", buildPrompt(song, lyrics)),
                ChatMessage("user", "请解释上面这首歌的歌词。"),
            ),
            responseFormat = ResponseFormat("text"),
            temperature = configs.temperature,
            topP = configs.topP,
            maxTokens = configs.maxTokens.takeIf { it > 0 },
            presencePenalty = configs.presencePenalty,
            frequencyPenalty = configs.frequencyPenalty
        )

        var connection: HttpURLConnection? = null
        try {
            val url = URL(apiUrl)
            Log.d(TAG, "Connecting to OpenAI compatible API: $apiUrl")

            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 60 * 1000
                readTimeout = 3 * (60 * 1000)
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer ${configs.apiKey}")
                setRequestProperty("User-Agent", "lyricon")
            }

            OutputStreamWriter(connection.outputStream).use {
                it.write(json.encodeToString(chatRequest))
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                val responseObj = json.decodeFromString<OpenAiChatResponse>(responseBody)
                responseObj.choices.firstOrNull()?.message?.content?.trim() ?: run {
                    Log.e(TAG, "Empty content in API response.")
                    return@withContext null
                }
            } else {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "API request failed with code $responseCode: $errorBody")
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: EOFException) {
            Log.w(TAG, "AI response stream ended unexpectedly: ${e.message ?: "EOF"}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Network or Parsing error in AiLyricExplainer: ${e.message}", e)
            null
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * 构建系统提示词：清晰说明任务目标，并要求以结构化方式返回解读。
     */
    private fun buildPrompt(song: Song?, lyrics: String): String {
        val title = song?.name?.takeIf { it.isNotBlank() } ?: "未知歌曲"
        val artist = song?.artist?.takeIf { it.isNotBlank() } ?: "未知歌手"

        // 只截取合理长度，避免超出上下文
        val excerpt = if (lyrics.length > 4000) lyrics.take(4000) else lyrics

        return """
你是资深乐评人与歌词解读专家。
请解读一段歌词的含义，帮助用户理解它的意境、情感与潜在隐喻。

# 元数据
歌曲："{title}"
歌手："{artist}"

# 歌词
$excerpt

# 输出要求
1. 使用简体中文，语言自然流畅，避免过度术语化。
2. 篇幅适中（约 200~500 字），先总述整体情感基调，再分段/分主题解读。
3. 如歌词意象明显，请解释其可能的隐喻与象征。
4. 不要编造歌词中不存在的内容，保持客观。
5. 直接输出解读正文，不要输出额外格式标记或标题。
""".trimIndent()
    }
}
