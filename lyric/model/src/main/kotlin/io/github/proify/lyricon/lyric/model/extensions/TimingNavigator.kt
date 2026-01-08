@file:Suppress("unused")

package io.github.proify.lyricon.lyric.model.extensions

import io.github.proify.lyricon.lyric.model.interfaces.ILyricTiming

/**
 * 根据播放时间查找歌词项
 * 支持缓存优化、线性探测、二分查找，以及重叠歌词处理
 */
class TimingNavigator<T : ILyricTiming>(source: List<T>) {

    val lyrics: List<T> = source
    val size: Int = lyrics.size

    /** 缓存最后匹配的索引，用于顺序查找优化 */
    var lastIndex: Int = 0

    /** 缓存最后请求的时间，用于判断播放方向 */
    var lastPosition: Long = -1L

    /**
     * 核心查找方法
     * 优化路径：缓存命中 -> 顺序线性探测 -> 二分查找
     * @return 匹配的歌词数量（考虑重叠）
     */
    inline fun findAt(position: Long, action: (T) -> Unit): Int {
        if (size == 0) return 0

        val idx = lastIndex

        // 1. 边界快速检查
        if (position < lyrics[0].begin) {
            lastIndex = 0
            lastPosition = position
            return 0
        }
        if (position > lyrics[size - 1].end) {
            lastIndex = size - 1
            lastPosition = position
            return 0
        }

        // 2. 顺序线性探测（优化连续播放场景）
        if (position >= lastPosition && idx < size) {
            val current = lyrics[idx]
            if (position in current.begin..current.end) {
                lastPosition = position
                return processOverlaps(position, idx, action)
            }

            val nextIdx = idx + 1
            if (nextIdx < size) {
                val next = lyrics[nextIdx]
                if (position in next.begin..next.end) {
                    lastIndex = nextIdx
                    lastPosition = position
                    return processOverlaps(position, nextIdx, action)
                }
            }
        }

        // 3. 二分查找（跳转场景）
        val matchIdx = binarySearchIndex(position)
        lastPosition = position
        if (matchIdx >= 0) {
            lastIndex = matchIdx
            return processOverlaps(position, matchIdx, action)
        }

        return 0
    }

    /**
     * 查找当前时间的歌词，如果找不到则返回前一条有效歌词
     */
    inline fun lookupOrPrevious(position: Long, action: (T) -> Unit): Int {
        val foundCount = findAt(position) { item -> action(item) }
        if (foundCount > 0) return foundCount

        val previous = findPreviousAt(position)
        if (previous != null) {
            action(previous)
            return 1
        }
        return 0
    }

    /**
     * 查找指定时间之前的最后一条歌词
     * @return 找到的歌词或 null
     */
    fun findPreviousAt(position: Long): T? {
        if (size == 0) return null

        if (position < lyrics[0].begin) return null
        if (position > lyrics[size - 1].end) return lyrics[size - 1]

        var low = 0
        var high = size - 1
        var resultIndex = -1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val item = lyrics[mid]

            if (item.begin < position) {
                resultIndex = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }

        return if (resultIndex >= 0) lyrics[resultIndex] else null
    }

    /**
     * 标准二分查找
     * @return 匹配索引或 -1
     */
    fun binarySearchIndex(position: Long): Int {
        var low = 0
        var high = size - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val item = lyrics[mid]

            when {
                position < item.begin -> high = mid - 1
                position > item.end -> low = mid + 1
                else -> return mid
            }
        }

        return -1
    }

    /**
     * 处理重叠歌词
     * 回调顺序遵循原数组顺序
     * @return 匹配的歌词数量
     */
    inline fun processOverlaps(position: Long, matchIndex: Int, action: (T) -> Unit): Int {
        var startIdx = matchIndex

        // 向前回溯，处理重叠的起始项
        while (startIdx > 0) {
            val prev = lyrics[startIdx - 1]
            if (position in prev.begin..prev.end) startIdx-- else break
        }

        var count = 0
        for (i in startIdx until size) {
            val item = lyrics[i]
            if (position < item.begin) break
            if (position <= item.end) {
                action(item)
                count++
            }
        }
        return count
    }

    /** 重置缓存 */
    fun reset() {
        lastIndex = 0
        lastPosition = -1L
    }
}