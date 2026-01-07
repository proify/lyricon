@file:Suppress("ReplacePrintlnWithLogging")

package com.makino.lyricon.model.lyric

import io.github.proify.lyricon.lyric.model.LyricLine
import io.github.proify.lyricon.lyric.model.extensions.filterByPosition
import io.github.proify.lyricon.lyric.model.extensions.filterByPositionOrPrevious
import org.junit.Test
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class ExampleUnitTest {

    @Test
    fun testLyricFilter() {
        var start = 0L
        val maxOverlap = 50L    // 最大允许重叠时间 5 秒
        val minDuration = 30L      // 每句歌词最短持续 3 秒
        val maxDuration = 100L     // 每句歌词最长持续 10 秒

        val lyrics = List(114514) { i ->

            // 随机生成每句歌词的时长
            val duration = Random.nextLong(minDuration, maxDuration)

            // 随机生成偏移，允许重叠
            val overlap = Random.nextLong(0, maxOverlap + 1)

            val line = LyricLine(
                begin = start,
                end = start + duration,
                text = "这是第 ${i + 1} 句歌词"
            )

            // 下一句开始时间可能会与当前句重叠
            start = line.end - overlap

            line
        }

        val totalDuration = lyrics.last().end

        println("=== 性能测试 ===")
        println("歌词总数: ${lyrics.size}")
        println("总时长: ${totalDuration / 1000}秒\n")

        testPosition(lyrics, 100, "开始位置")
        testPosition(lyrics, lyrics.last().end / 2, "中间位置")
        testPosition(lyrics, lyrics.last().end, "结束位置")

        testPosition(lyrics, 100, "开始位置（线性搜索）", true)
        testPosition(lyrics, lyrics.last().end / 2, "中间位置（线性搜索）", true)
        testPosition(lyrics, lyrics.last().end, "结束位置（线性搜索）", true)

        fun batchSearch(pos: LongArray, linearSearch: Boolean = false) {
            println()
            println("=== 批量测试 ===")
            println("在${lyrics.size} 个歌词中查找, 查找次数: ${pos.size}, 是否线性搜索: $linearSearch")
            val time = measureTimeMillis {
                for (i in pos) {
                    lyrics.filterByPosition(i, linearSearch)
                }
            }
            println("总耗时: ${time}ms")
        }

        val pos = List(10000) {
            Random.nextLong(0, totalDuration)
        }.toLongArray()

        fun testfilterByPositionOrPrevious() {
            println("testFilterByPositionOrPrevious")
            val time = measureTimeMillis {
                for (i in pos) {
                    lyrics.filterByPositionOrPrevious(i)
                }
            }
            println("总耗时: ${time}ms")
        }
        testfilterByPositionOrPrevious()

        batchSearch(pos)
        batchSearch(pos, true)
        println()
    }

    private fun testPosition(
        lyrics: List<LyricLine>,
        position: Long,
        label: String,
        linearSearch: Boolean = false
    ) {
        val start = System.currentTimeMillis()
        val current = lyrics.filterByPosition(position, linearSearch)
        println("[$label] 歌词: ${current.map { it.text }}, 耗时: ${System.currentTimeMillis() - start}ms")
    }
}