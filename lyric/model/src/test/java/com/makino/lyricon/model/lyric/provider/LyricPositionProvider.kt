@file:Suppress("unused")

package com.makino.lyricon.model.lyric.provider

import io.github.proify.lyricon.lyric.model.interfaces.ILyricTiming

class LyricPositionProvider(
    source: List<ILyricTiming>
) {

    /** 扁平化数组，减少集合框架开销 */
    val elements: Array<ILyricTiming> = source.toTypedArray()
    val count: Int = elements.size

    /** 缓存最后一次匹配的索引，用于线性探测优化 */
    var cacheIndex: Int = 0

    /** 缓存最后一次请求的时间戳，用于判断播放方向 */
    var lastSeekPosition: Long = -1L

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
     * 根据位置查找，如果没找到当前匹配的歌词，就返回上一个符合的歌词
     * 适用于显示当前播放歌词或上一句歌词的场景
     */
    inline fun findWithPrevious(position: Long, action: (ILyricTiming) -> Unit): Int {
        val size = count
        if (size == 0) return 0

        // 先尝试标准查找
        val foundCount = find(position) { item ->
            action(item)
        }

        // 如果找到了匹配的歌词，直接返回
        if (foundCount > 0) {
            return foundCount
        }

        // 如果没有找到匹配的歌词，查找上一个符合的歌词
        val previousItem = findPrevious(position)
        if (previousItem != null) {
            action(previousItem)
            return 1
        }

        return 0
    }

    /**
     * 查找指定位置之前的最后一个歌词项
     * @param position 当前时间位置
     * @return 上一个歌词项，如果不存在则返回null
     */
    fun findPrevious(position: Long): ILyricTiming? {
        val size = count
        if (size == 0) return null

        val arr = elements

        // 如果位置小于第一个歌词的开始时间，没有上一个歌词
        if (position < arr[0].begin) {
            return null
        }

        // 如果位置大于最后一个歌词的结束时间，返回最后一个歌词
        if (position > arr[size - 1].end) {
            return arr[size - 1]
        }

        // 使用二分查找找到第一个开始时间大于position的歌词项，然后返回它的前一项
        var low = 0
        var high = size - 1
        var resultIndex = -1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val item = arr[mid]

            if (item.begin < position) {
                resultIndex = mid  // 记录当前找到的符合条件的索引
                low = mid + 1      // 继续向右查找更大的
            } else {
                high = mid - 1     // 向左查找
            }
        }

        return if (resultIndex >= 0) arr[resultIndex] else null
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

        // 向前回溯：处理由于重叠导致的"起始项"被跳过的情况
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