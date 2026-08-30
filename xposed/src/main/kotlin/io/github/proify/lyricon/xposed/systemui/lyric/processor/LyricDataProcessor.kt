/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.lyric.processor

import io.github.proify.lyricon.lyric.model.LyricWord
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.lyric.style.BasicStyle
import io.github.proify.lyricon.lyric.style.LyricStyle
import io.github.proify.lyricon.xposed.logger.YLog
import io.github.proify.lyricon.xposed.systemui.lyric.LyricPrefs
import io.github.proify.lyricon.xposed.systemui.util.ChineseConverter.toSimplified
import io.github.proify.lyricon.xposed.systemui.util.ChineseConverter.toTraditional
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 歌词数据加工引擎
 * 负责执行前置清洗、后置增强与显示层变换。
 */
object LyricDataProcessor {

    private const val TAG = "LyricDataProcessor"

    /** 注册后置加工插件列表 */
    private val postProcessors = listOf(
        AiTranslationPostProcessor()
    ).sortedBy { it.priority }

    /**
     * 执行前置加工。
     * 职责：处理繁简转换、屏蔽词等基础数据清洗。
     */
    fun executePreProcessing(song: Song): Song {
        return song
            .let(::filterBlockedWords)
            .let(::convertChineseCharacters)
    }

    /**
     * 执行后置加工流水线 (异步)
     * 职责：顺序等待所有已启用的耗时插件处理完毕，合并结果后统一返回。
     */
    suspend fun executePostProcessingPipeline(song: Song, style: LyricStyle): Song =
        withContext(Dispatchers.Default) {
            var currentSong = song
            postProcessors.forEach { processor ->
                if (processor.isEnabled(style)) {
                    try {
                        currentSong = processor.process(currentSong, style)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        YLog.error(
                            tag = TAG,
                            msg = "Processor ${processor::class.java.simpleName} failed",
                            e = e
                        )
                    }
                }
            }
            currentSong
        }

    /**
     * 执行显示层加工。
     * 职责：应用只影响展示、不应参与 AI 等数据增强步骤的变换。
     */
    fun executeDisplayProcessing(song: Song, style: LyricStyle): Song {
        return applyTranslationOnly(song, style)
    }

    // --- 内部基础处理方法 ---

    private fun filterBlockedWords(song: Song): Song {
        val regex = LyricPrefs.baseStyle.blockedWordsRegex ?: return song
        return song.copy(lyrics = song.lyrics?.filter { line ->
            line.text?.let { !regex.containsMatchIn(it) } ?: true
        })
    }

    private fun applyTranslationOnly(song: Song, style: LyricStyle): Song {
        if (!style.packageStyle.text.isTranslationOnly) return song

        return song.copy(lyrics = song.lyrics?.map { line ->
            if (!line.translation.isNullOrBlank()) {
                line.copy(
                    text = line.translation,
                    words = line.translationWords,
                    translation = null,
                    translationWords = null
                )
            } else {
                line
            }
        })
    }

    private fun convertChineseCharacters(song: Song): Song {
        val mode = LyricPrefs.baseStyle.chineseConversionMode
        if (mode == BasicStyle.CHINESE_CONVERSION_OFF) return song

        val convert: (String?) -> String? = when (mode) {
            BasicStyle.CHINESE_CONVERSION_TRADITIONAL -> { v -> v?.toTraditional() }
            BasicStyle.CHINESE_CONVERSION_SIMPLIFIED -> { v -> v?.toSimplified() }
            else -> { v -> v }
        }

        val convertWord: (List<LyricWord>?) -> List<LyricWord>? = { words ->
            words?.map { word ->
                word.copy(text = convert(word.text))
            }
        }

        return song.copy(
            name = convert(song.name),
            artist = convert(song.artist),
            lyrics = song.lyrics?.map { line ->
                line.copy(
                    words = convertWord(line.words),
                    translationWords = convertWord(line.translationWords),
                    secondaryWords = convertWord(line.secondaryWords),
                )
            }
        ).normalize()
    }
}
