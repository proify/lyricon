@file:Suppress("unused")

package com.makino.lyricon.model.lyric.provider.v2

import com.makino.lyricon.model.lyric.provider.ILyricPositionProvider
import io.github.proify.lyricon.lyric.model.interfaces.ILyricTiming

/**
 * V3 歌词位置提供者
 * 针对音频播放场景优化：高频调用、时间线性递增、可能存在歌词重叠。
 */
class V2(
    source: List<ILyricTiming>
) : ILyricPositionProvider {

    /** 扁平化数组，减少集合框架开销 */
    val elements: Array<ILyricTiming> = source.toTypedArray()
    val count: Int = elements.size

    /** 缓存最后一次匹配的索引，用于线性探测优化 */
    var cacheIndex: Int = 0

    /** 缓存最后一次请求的时间戳，用于判断播放方向 */
    var lastSeekPosition: Long = -1L

    override fun testfind(position: Long, action: (ILyricTiming) -> Unit): Int {
        return find(position, action)
    }

    /**
     * 核心查找算法
     * 优化路径：命中缓存 -> 顺序线性探测 -> 二分查找
     */
    inline fun find(position: Long, action: (ILyricTiming) -> Unit): Int {
        val size = count
        if (size == 0) return 0

        val arr = elements
        val idx = cacheIndex

        // 1. 边界快速检查 (Boundary Check)
        if (position < arr[0].begin) {
            cacheIndex = 0
            lastSeekPosition = position
            return 0
        }
        if (position > arr[size - 1].end) {
            cacheIndex = size - 1
            lastSeekPosition = position
            return 0
        }

        // 2. 线性探测 (Linear Probing / Forward Search)
        // 针对 99% 的正常播放场景：当前位置就在 cacheIndex 或其后方不远处
        if (position >= lastSeekPosition && idx < size) {
            // 检查当前缓存位置
            val current = arr[idx]
            if (position >= current.begin) {
                if (position <= current.end) {
                    // 命中缓存项，处理可能的重叠并返回
                    lastSeekPosition = position
                    return resolveOverlaps(position, idx, action)
                }

                // 尝试向后线性探测 2 个位置（覆盖短歌词重叠或快速步进）
                val nextIdx = idx + 1
                if (nextIdx < size) {
                    val next = arr[nextIdx]
                    if (position >= next.begin && position <= next.end) {
                        cacheIndex = nextIdx
                        lastSeekPosition = position
                        return resolveOverlaps(position, nextIdx, action)
                    }
                }
            }
        }

        // 3. 全局搜索 (Binary Search)
        // 当发生 Seek（进度条跳转）或探测失败时使用二分查找
        val matchIdx = binarySearch(position)
        lastSeekPosition = position

        if (matchIdx >= 0) {
            cacheIndex = matchIdx
            return resolveOverlaps(position, matchIdx, action)
        }

        return 0
    }

    /**
     * 执行标准的二分查找
     * @return 匹配的索引，若无匹配则返回 -1
     */
    fun binarySearch(position: Long): Int {
        var low = 0
        var high = count - 1
        val arr = elements

        while (low <= high) {
            val mid = (low + high) ushr 1
            val item = arr[mid]

            when {
                position < item.begin -> high = mid - 1
                position > item.end -> low = mid + 1
                else -> return mid
            }
        }
        return -1
    }

    /**
     * 处理歌词重叠 (Overlap Resolution)
     * 考虑到歌词数据通常在 index 上是有序的，但 begin/end 可能交织。
     * 该方法保证了回调顺序严格遵循数组原始顺序。
     */
    inline fun resolveOverlaps(
        position: Long,
        matchIdx: Int,
        action: (ILyricTiming) -> Unit
    ): Int {
        val arr = elements
        val size = count

        // 向前回溯：处理由于重叠导致的“起始项”被跳过的情况
        var startIdx = matchIdx
        while (startIdx > 0) {
            val prev = arr[startIdx - 1]
            if (position in prev.begin..prev.end) {
                startIdx--
            } else {
                break
            }
        }

        // 顺序向后扫描并执行回调
        var resultCount = 0
        for (i in startIdx until size) {
            val item = arr[i]
            // 如果当前项 begin 已经超过 position，后续项由于有序性必不匹配
            if (position < item.begin) break

            if (position <= item.end) {
                action(item)
                resultCount++
            }
        }
        return resultCount
    }

    fun reset() {
        cacheIndex = 0
        lastSeekPosition = -1L
    }
}