/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.ai.translate

import android.util.Log
import io.github.proify.lyricon.lyric.ai.core.AiConfig
import io.github.proify.lyricon.lyric.model.Song
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.ArrayDeque
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * 有界后台调度器：应对暴力切歌场景下的 AI 请求。
 *
 * - 同 key 复用（同一首歌重复请求只发一次）；
 * - pending/running 双上限，超出淘汰最早 pending；
 * - 派发采用 LIFO（最新排队的歌先跑，契合"最新播放优先"），
 *   淘汰采用 FIFO（最早就队的先被丢弃），两种语义有意区分；
 * - 清空缓存（generation 失效）会取消全部 pending，且运行中结果既不写缓存
 *   也不回送给调用方，避免清缓存后仍应用陈旧译文。
 */
internal class AiTranslationScheduler(
    private val cache: AiTranslationCache,
    private val generation: AtomicInteger,
    private val maxRunning: Int,
    private val maxPending: Int
) {
    private companion object {
        const val TAG = "LyriconAiTranslator"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val lock = Any()
    private val jobs = ConcurrentHashMap<String, TranslationJob>()
    private val pending = ArrayDeque<TranslationJob>()
    private var running = 0

    fun getOrEnqueue(
        key: String,
        configs: AiConfig,
        options: AiTranslationOptions,
        song: Song,
        originalLines: List<String>
    ): Deferred<List<TranslationItem>?> {
        synchronized(lock) {
            jobs[key]?.let {
                Log.d(TAG, "Reusing scheduled AI translation for: ${song.name} [$key]")
                return it.deferred
            }

            val job = TranslationJob(
                key = key,
                songName = song.name.orEmpty(),
                configs = configs,
                options = options,
                song = song,
                originalLines = originalLines,
                generation = generation.get()
            )
            jobs[key] = job
            pending.addLast(job)
            Log.i(
                TAG,
                "Queued AI translation: ${job.songName} pending=${pending.size}, running=$running"
            )
            trimPendingLocked()
            dispatchNextLocked()
            return job.deferred
        }
    }

    fun cancelPending() {
        synchronized(lock) {
            while (pending.isNotEmpty()) {
                val job = pending.removeFirst()
                job.state = TranslationJobState.CANCELLED
                jobs.remove(job.key, job)
                job.deferred.complete(null)
                Log.d(TAG, "Cancelled pending AI translation: ${job.songName}")
            }
        }
    }

    private fun trimPendingLocked() {
        while (pending.size > maxPending) {
            val dropped = pending.removeFirst()
            if (dropped.state != TranslationJobState.PENDING) continue

            dropped.state = TranslationJobState.CANCELLED
            jobs.remove(dropped.key, dropped)
            dropped.deferred.complete(null)
            Log.w(TAG, "Dropped pending AI translation: ${dropped.songName}, reason=queue_full")
        }
    }

    private fun dispatchNextLocked() {
        while (running < maxRunning && pending.isNotEmpty()) {
            val job = pending.removeLast()
            if (job.state != TranslationJobState.PENDING) continue

            job.state = TranslationJobState.RUNNING
            running++
            Log.i(
                TAG,
                "Running AI translation: ${job.songName} pending=${pending.size}, running=$running"
            )
            scope.launch { runJob(job) }
        }
    }

    private suspend fun runJob(job: TranslationJob) {
        try {
            val apiResults =
                AiTranslationRequester.request(job.configs, job.options, job.song, job.originalLines)
            val isCurrentGeneration = job.generation == generation.get()
            if (!apiResults.isNullOrEmpty() && isCurrentGeneration) {
                Log.i(TAG, "AI translation completed. Saving to cache: ${job.songName}")
                cache.putMemory(job.key, apiResults)
                cache.saveToDb(job.key, apiResults)
            } else if (!isCurrentGeneration) {
                Log.w(TAG, "Dropping stale AI translation result: ${job.songName} (cache cleared)")
            }
            job.state = TranslationJobState.COMPLETED
            // 缓存被清空后（generation 失效）不再回送陈旧结果，等待方按失败处理
            job.deferred.complete(if (isCurrentGeneration) apiResults else null)
        } catch (e: CancellationException) {
            job.state = TranslationJobState.CANCELLED
            job.deferred.cancel(e)
            throw e
        } catch (e: Exception) {
            job.state = TranslationJobState.COMPLETED
            Log.e(TAG, "AI translation job failed for [${job.songName}]: ${e.message}", e)
            job.deferred.complete(null)
        } finally {
            synchronized(lock) {
                running = (running - 1).coerceAtLeast(0)
                jobs.remove(job.key, job)
                dispatchNextLocked()
            }
        }
    }

    private data class TranslationJob(
        val key: String,
        val songName: String,
        val configs: AiConfig,
        val options: AiTranslationOptions,
        val song: Song,
        val originalLines: List<String>,
        val generation: Int,
        val deferred: CompletableDeferred<List<TranslationItem>?> = CompletableDeferred(),
        var state: TranslationJobState = TranslationJobState.PENDING
    )

    private enum class TranslationJobState {
        PENDING,
        RUNNING,
        COMPLETED,
        CANCELLED
    }
}
