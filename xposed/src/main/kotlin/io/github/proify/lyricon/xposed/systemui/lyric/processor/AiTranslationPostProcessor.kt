/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.lyric.processor

import android.util.Log
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.lyric.model.extensions.deepCopy
import io.github.proify.lyricon.lyric.style.LyricStyle
import io.github.proify.lyricon.xposed.systemui.ai.translate.AiTranslationOptions
import io.github.proify.lyricon.xposed.systemui.ai.translate.AiTranslator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * AI 翻译后处理器。
 *
 * 该处理器负责在歌词数据渲染前进行最后的检查与增强。
 * 如果检测到歌词缺少翻译，将根据配置调用外部 AI 服务进行在线异步翻译。
 *
 * 处理流程：
 * 1. 检查总开关及配置可用性。
 * 2. 检查是否满足“自动跳过中文歌”条件。
 * 3. 检查歌词是否已经包含完整翻译（避免重复翻译）。
 * 4. 调用 [AiTranslator] 进行同步翻译并返回新对象。
 */
class AiTranslationPostProcessor : PostProcessor {

    companion object {
        private const val TAG = "AiTranslationPostProcessor"
    }

    /**
     * 处理器优先级。
     * 设置为 10，确保其在基础解析器之后、视觉样式处理器之前运行。
     */
    override val priority: Int = 10

    /**
     * 判断当前样式配置下是否允许执行 AI 翻译。
     *
     * @param style 当前应用的歌词样式。
     * @return 如果功能已启用且配置有效（如 API Key 已填）则返回 true。
     */
    override fun isEnabled(style: LyricStyle): Boolean {
        val targetStyle = style.basicStyle
        val enabled = targetStyle.isAiTranslationEnable
                && targetStyle.aiConfigs?.isUsable == true
        if (!enabled) {
            Log.v(TAG, "Processor disabled: Config unusable or switch turned off.")
        }
        return enabled
    }

    /**
     * 执行翻译处理核心逻辑。
     *
     * 该方法会在协程中运行，通过 [AiTranslator] 发起 IO 请求。
     * 为保证数据一致性，在翻译前会进行 [Song] 对象的快照比对，若翻译结果无变化则返回原对象。
     *
     * @param song 原始歌曲数据对象。
     * @param style 当前关联的样式。
     * @return 处理后的歌曲对象（若无需翻译或翻译失败则返回原对象）。
     */
    override suspend fun process(song: Song, style: LyricStyle): Song {
        if (song.lyrics.isNullOrEmpty()) {
            Log.d(TAG, "Skip process: Lyrics is null or empty.")
            return song
        }

        // 处理自动忽略中文逻辑
        if (style.basicStyle.isAiTranslationAutoIgnoreChinese && song.isFullyChinese()) {
            Log.d(
                TAG,
                "Skip process: Song [${song.name}] is fully Chinese and auto-ignore is enabled."
            )
            return song
        }

        // 如果所有行都有翻译，则不再消耗 API 额度
        if (isFullyTranslated(song)) {
            Log.d(TAG, "Skip process: Song [${song.name}] is already fully translated.")
            return song
        }

        val translationConfig = style.basicStyle.aiConfigs ?: run {
            Log.w(TAG, "Abort process: aiConfigs is null.")
            return song
        }
        // 翻译业务参数：目标语言 / 风格要求（来自样式配置，与连接配置分离）
        val translationOptions = AiTranslationOptions(
            targetLanguage = style.basicStyle.aiTranslationTargetLanguage,
            stylePrompt = style.basicStyle.aiTranslationPrompt
        )

        val snapshotLyrics = song.lyrics?.deepCopy()

        return withContext(Dispatchers.IO) {
            Log.i(TAG, "Starting AI translation for: ${song.name} - ${song.artist}")

            val translatedSong = try {
                AiTranslator.translateSongSync(song, translationConfig, translationOptions)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI translation failed for [${song.name}]: ${e.message}")
                return@withContext song
            }

            // 变更检测：如果翻译内容与原内容一致，返回旧引用以优化内存和 UI 刷新
            if (translatedSong.lyrics == snapshotLyrics) {
                Log.d(TAG, "Translation completed but no content changed for [${song.name}].")
                return@withContext song
            }

            Log.i(TAG, "Successfully translated: ${song.name}")
            return@withContext translatedSong
        }
    }

    /**
     * 内部辅助：检查歌曲的所有歌词行是否均已填充有效翻译。
     */
    private fun isFullyTranslated(song: Song): Boolean {
        return song.lyrics?.all { lyricLine ->
            !lyricLine.translation.isNullOrBlank()
        } ?: false
    }

    /**
     * 内部辅助：判断歌词文本是否主要由中文字符组成（忽略空白和标点）。
     */
    private fun Song.isFullyChinese(): Boolean {
        return lyrics?.all { line ->
            line.text?.filterNot { it.isWhitespace() || it.isPunctuation() }
                ?.all { it.isChinese() } ?: true
        } ?: true
    }

    /**
     * 字符级中文判定。
     * 使用 Unicode 字符块识别常见汉字及符号。
     */
    private fun Char.isChinese(): Boolean {
        val ub = Character.UnicodeBlock.of(this)
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
                ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
                ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B ||
                ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION ||
                ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS ||
                ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
    }

    /**
     * 判定字符是否为标点符号或非字母数字字符。
     */
    private fun Char.isPunctuation(): Boolean {
        return !isLetterOrDigit() && !isWhitespace()
    }
}
