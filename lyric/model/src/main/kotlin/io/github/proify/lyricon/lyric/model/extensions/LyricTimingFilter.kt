@file:Suppress("unused")

package io.github.proify.lyricon.lyric.model.extensions

import io.github.proify.lyricon.lyric.model.interfaces.ILyricTiming

/**
 * 根据给定的时间戳筛选歌词。
 *
 * 该方法假定列表已按起始时间 [ILyricTiming.begin] 升序排列。
 * 支持处理重叠歌词（如双语字幕或和声），返回所有包含该位置的条目。
 *
 * @param position 当前播放位置（毫秒）。
 * @param linearSearch 是否强制使用线性扫描。默认在列表较小时自动启用以优化内存分配。
 * @return 包含该位置的歌词列表；若未找到则返回空列表。
 */
fun <T : ILyricTiming> List<T>.filterByPosition(
    position: Long,
    linearSearch: Boolean = false
): List<T> {
    if (isEmpty()) return emptyList()

    // 快速失败处理：如果位置超出了列表的总时间范围，直接跳过计算
    if (position < this[0].begin || position > this[size - 1].end) {
        return emptyList()
    }

    // 策略选择：对于小样本量，线性扫描由于更好的 CPU 缓存局部性，性能往往优于二分查找
    if (linearSearch || size < 50) {
        val result = ArrayList<T>(1)
        for (i in 0 until size) {
            val item = this[i]
            if (position in item.begin..item.end) {
                result.add(item)
            } else if (item.begin > position) {
                // 基于列表有序性的早期剪枝：后续条目的起始时间必大于当前位置
                break
            }
        }
        return result
    }

    return filterByPositionBinaryInternal(position)
}

/**
 * 查找当前播放位置对应的歌词，若当前位置为空白期，则返回上一句歌词。
 *
 * @param position 当前播放位置。
 * @return 命中位置的歌词列表，或最后一句已结束的歌词；若列表为空或尚未到达第一句，则返回空。
 */
fun <T : ILyricTiming> List<T>.filterByPositionOrPrevious(position: Long): List<T> {
    if (isEmpty()) return emptyList()

    // 边界情况：早于第一句歌词
    if (position < this[0].begin) return emptyList()
    // 边界情况：晚于最后一句歌词，返回最后一个元素
    if (position > this[size - 1].end) return listOf(last())

    // 核心算法：使用二分查找定位索引
    var low = 0
    var high = size - 1

    while (low <= high) {
        val mid = (low + high) ushr 1
        val item = this[mid]

        when {
            position < item.begin -> high = mid - 1
            position > item.end -> low = mid + 1
            else -> {
                // 精确匹配：找到包含该时间点的歌词条目，向两侧扩散处理重叠情况
                return getOverlappingRange(position, mid)
            }
        }
    }

    // 间隙处理：当循环结束未命中(mid)时，high 指针指向的是 position 之前的最后一个条目
    return if (high >= 0) {
        listOf(this[high])
    } else {
        emptyList()
    }
}

/**
 * 内部封装的二分查找逻辑。
 * 时间复杂度: O(log n)，在发生大规模重叠时会退化至 O(log n + k)。
 */
private fun <T : ILyricTiming> List<T>.filterByPositionBinaryInternal(position: Long): List<T> {
    var low = 0
    var high = size - 1

    while (low <= high) {
        val mid = (low + high) ushr 1
        val item = this[mid]

        when {
            position < item.begin -> high = mid - 1
            position > item.end -> low = mid + 1
            else -> return getOverlappingRange(position, mid)
        }
    }
    return emptyList()
}

/**
 * 从指定索引向前后扩散，获取所有覆盖 [position] 的连续条目。
 * 解决 LRC 规范中可能出现的双语重叠或多行同时间点显示的问题。
 */
private fun <T : ILyricTiming> List<T>.getOverlappingRange(position: Long, matchIdx: Int): List<T> {
    var start = matchIdx
    var end = matchIdx

    // 向左探测
    while (start > 0) {
        val prev = this[start - 1]
        if (position in prev.begin..prev.end) start-- else break
    }
    // 向右探测
    while (end < size - 1) {
        val next = this[end + 1]
        if (position in next.begin..next.end) end++ else break
    }

    return if (start == end) {
        listOf(this[start])
    } else {
        // 使用视图子列表避免不必要的内存拷贝
        subList(start, end + 1)
    }
}

/**
 * 筛选完全包含在指定时间范围内的歌词条目。
 * * @param start 范围起始时间（含）。
 * @param end 范围结束时间（含）。
 * @return 符合条件的歌词列表。
 */
fun <T : ILyricTiming> List<T>.filterByRange(start: Long, end: Long): List<T> {
    if (isEmpty()) return emptyList()
    val result = ArrayList<T>()
    for (i in 0 until size) {
        val item = this[i]
        // 判定条件：条目必须完全落在给定闭区间内
        if (item.begin >= start && item.end <= end) {
            result.add(item)
        } else if (item.begin > end) {
            // 排序列表优化：起始时间已超过搜索范围上限，终止扫描
            break
        }
    }
    return result
}