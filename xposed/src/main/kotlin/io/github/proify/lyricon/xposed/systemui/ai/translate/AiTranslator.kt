/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.ai.translate

import android.content.Context
import android.util.Log
import io.github.proify.lyricon.lyric.ai.core.AiConfig
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.xposed.systemui.ai.translate.AiTranslator.translateSongSync
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.atomic.AtomicInteger

/**
 * SystemUI 进程内的 AI 歌词翻译门面。
 *
 * 职责边界：
 * - [AiTranslationKey] 生成整首歌级缓存 key；
 * - [AiTranslationCache] 内存 + SQLite 双级缓存；
 * - [AiTranslationScheduler] 暴力切歌场景下的 pending/running 调度、同 key 复用与队列淘汰；
 * - [AiTranslationRequester] 筛选行、组请求并解析响应；
 * - [AiTranslationApplicator] 把有效译文写回 Song。
 *
 * AI 网络请求不绑定歌词 UI 流水线生命周期。切歌只会取消当前等待者，不会取消已经发出的请求；
 * 已开始的请求完成后仍会写入缓存，未开始的 pending 请求会在队列过长或清空缓存时被淘汰。
 */
object AiTranslator {
    private const val TAG = "LyriconAiTranslator"
    private const val MAX_CACHE_SIZE = 1000
    private const val MAX_RUNNING_TRANSLATIONS = 10
    private const val MAX_PENDING_TRANSLATIONS = 10

    private val cacheGeneration = AtomicInteger(0)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val cache = AiTranslationCache(
        maxCacheSize = MAX_CACHE_SIZE,
        generation = cacheGeneration,
        scope = scope
    )
    private val scheduler = AiTranslationScheduler(
        cache = cache,
        generation = cacheGeneration,
        maxRunning = MAX_RUNNING_TRANSLATIONS,
        maxPending = MAX_PENDING_TRANSLATIONS
    )

    fun init(context: Context) {
        cache.init(context)
    }

    suspend fun translateSongSync(
        song: Song,
        configs: AiConfig,
        options: AiTranslationOptions = AiTranslationOptions()
    ): Song {
        if (!configs.isUsable) {
            Log.w(TAG, "Translation skipped: Configs not usable (missing API Key or disabled).")
            return song
        }
        if (song.lyrics.isNullOrEmpty()) return song

        return try {
            translateSong(song, configs, options)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Critical error during translateSongSync: ${e.message}", e)
            song
        }
    }

    /** Kept public for the existing network test. Production flow should go through [translateSongSync]. */
    suspend fun doOpenAiRequest(
        configs: AiConfig,
        song: Song? = null,
        texts: List<String>
    ): List<TranslationItem>? =
        AiTranslationRequester.request(configs, AiTranslationOptions(), song, texts)

    fun clearCache(callback: () -> Unit) {
        Log.i(TAG, "Clearing all translation caches (Memory & DB)...")
        scheduler.cancelPending()
        cache.clear(callback)
    }

    private suspend fun translateSong(
        song: Song,
        configs: AiConfig,
        options: AiTranslationOptions
    ): Song {
        val currentLyrics = song.lyrics ?: return song
        val originalLines = currentLyrics.map { it.text?.trim() ?: "" }
        val songContentId = AiTranslationKey.calculate(configs, options, song, originalLines)

        cache.getFromMemory(songContentId)?.let {
            Log.d(TAG, "Memory cache hit for: ${song.name} [$songContentId]")
            return AiTranslationApplicator.apply(song, it)
        }

        cache.getFromDb(songContentId)?.let {
            Log.d(TAG, "Database cache hit for: ${song.name} [$songContentId]")
            cache.putMemory(songContentId, it)
            return AiTranslationApplicator.apply(song, it)
        }

        Log.i(
            TAG,
            "Cache miss. Waiting for AI translation: ${song.name} (${originalLines.size} lines)"
        )
        val apiResults =
            scheduler.getOrEnqueue(songContentId, configs, options, song, originalLines).await()
        if (apiResults.isNullOrEmpty()) {
            Log.w(TAG, "Failed to get translation from API.")
            return song
        }
        return AiTranslationApplicator.apply(song, apiResults)
    }
}
