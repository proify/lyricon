package io.github.proify.lyricon.lyric.model

private const val THRESHOLD_SMALL_LIST = 50     // 小列表阈值，小于此值直接遍历
private const val THRESHOLD_HEAD_RATIO = 0.25   // 头部线性查找比例
private const val THRESHOLD_TAIL_RATIO = 0.85  // 尾部线性查找比例

/**
 * 查找当前播放位置对应的歌词
 *
 * @param position 当前播放位置(毫秒)
 * @return 匹配的歌词列表
 */
fun <T : ILyricTiming> List<T>.filterByPosition(position: Int): List<T> {
    if (isEmpty()) return emptyList()

    // 1. 边界检查（快速失败）：如果位置在第一句之前或最后一句之后
    val first = this[0]
    val last = this[size - 1]

    // 注意：最后一句之后可能还需要显示最后一句（取决于业务逻辑），这里保守处理：
    // 如果还没开始播放第一句，且第一句开始时间 > 0，则肯定为空
    if (position < first.begin) return emptyList()
    // 如果超过了最后一句的结束时间，肯定为空
    if (position > last.end) return emptyList()

    // 2. 小列表直接线性查找 (CPU 缓存友好)
    if (size < THRESHOLD_SMALL_LIST) {
        return filterByLinearScan(position)
    }

    // 3. 计算进度比例
    val totalDuration = last.end
    // 避免除以零异常
    val progress = if (totalDuration > 0) position.toDouble() / totalDuration else 0.0

    // 4. 混合策略选择
    return when {
        // 头部：大部分人从头听，前向线性查找命中率高且缓存友好
        progress < THRESHOLD_HEAD_RATIO -> filterByPositionForward(position)
        // 尾部：后向查找
        progress > THRESHOLD_TAIL_RATIO -> filterByPositionBackward(position)
        // 中段：二分查找
        else -> filterByPositionBinary(position)
    }
}

/**
 * 查找当前或上一句歌词（用于空窗期显示上一句）
 *
 * @param position 当前播放位置
 * @return 匹配的歌词列表
 */
fun <T : ILyricTiming> List<T>.filterByPositionOrPrevious(position: Int): List<T> {
    if (isEmpty()) return emptyList()

    // 1. 尝试精确查找
    val current = filterByPosition(position)
    if (current.isNotEmpty()) return current

    // 2. 查找上一句（即寻找 end < position 的最大值）
    // 如果位置大于最后一句的结束时间，直接返回最后一句
    if (position > last().end) return listOf(last())
    // 如果位置小于第一句开始时间，确实没有上一句
    if (position < first().begin) return emptyList()

    // 3. 二分查找上一句
    val previous = findPreviousLyricBinary(position)
    return if (previous != null) listOf(previous) else emptyList()
}

// ==================== 内部算法实现 ====================

/**
 * 通用线性扫描 (适用于小列表)
 */
private fun <T : ILyricTiming> List<T>.filterByLinearScan(position: Int): List<T> {
    // 预估一般只有1行，设置小容量减少扩容
    val result = ArrayList<T>(1)
    for (i in indices) {
        val item = this[i]
        if (position in item.begin..item.end) {
            result.add(item)
        }
    }
    return result
}

/**
 * 头部优化：前向查找
 */
private fun <T : ILyricTiming> List<T>.filterByPositionForward(position: Int): List<T> {
    var startIdx = -1
    var endIdx = -1

    for (i in indices) {
        val item = this[i]
        // 剪枝：如果当前句开始时间已超过 position，后面不可能匹配了（前提是列表按时间排序）
        if (item.begin > position) break

        if (position <= item.end) { //隐含条件 item.begin <= position
            if (startIdx == -1) startIdx = i
            endIdx = i
        }
    }

    if (startIdx == -1) return emptyList()
    // 返回视图，避免拷贝
    return subList(startIdx, endIdx + 1)
}

/**
 * 尾部优化：后向查找
 */
private fun <T : ILyricTiming> List<T>.filterByPositionBackward(position: Int): List<T> {
    var startIdx = -1
    var endIdx = -1

    // 从后往前遍历
    for (i in indices.reversed()) {
        val item = this[i]
        // 剪枝：如果当前句结束时间小于 position，前面不可能匹配了
        if (item.end < position) break

        if (position >= item.begin) { // 隐含条件 item.end >= position
            if (endIdx == -1) endIdx = i
            startIdx = i
        }
    }

    if (startIdx == -1) return emptyList()
    return subList(startIdx, endIdx + 1)
}

/**
 * 中段优化：二分查找
 * 核心逻辑：先二分找到任意一个匹配点，然后向左右扩散寻找重叠行
 */
private fun <T : ILyricTiming> List<T>.filterByPositionBinary(position: Int): List<T> {
    var left = 0
    var right = size - 1
    var matchIndex = -1

    // 标准二分查找定位
    while (left <= right) {
        val mid = (left + right) ushr 1 // 无符号右移，防止整型溢出
        val item = this[mid]

        when {
            position < item.begin -> right = mid - 1
            position > item.end -> left = mid + 1
            else -> {
                matchIndex = mid
                break // 找到其中一个匹配项，跳出循环
            }
        }
    }

    if (matchIndex == -1) return emptyList()

    // 向左扩散：寻找起始点
    var start = matchIndex
    while (start > 0) {
        val prev = this[start - 1]
        if (position in prev.begin..prev.end) {
            start--
        } else {
            break
        }
    }

    // 向右扩散：寻找结束点
    var end = matchIndex
    while (end < size - 1) {
        val next = this[end + 1]
        if (position in next.begin..next.end) {
            end++
        } else {
            break
        }
    }

    return subList(start, end + 1)
}

/**
 * 二分查找上一句 (即查找 end < position 的最大值)
 */
private fun <T : ILyricTiming> List<T>.findPreviousLyricBinary(position: Int): T? {
    var left = 0
    var right = size - 1
    var candidateIndex = -1

    while (left <= right) {
        val mid = (left + right) ushr 1
        val item = this[mid]

        if (item.end < position) {
            // 当前项在 position 之前，可能是候选者
            candidateIndex = mid
            // 继续向右尝试找更靠后的
            left = mid + 1
        } else {
            // 当前项在 position 之后或包含 position，上一句肯定在左边
            right = mid - 1
        }
    }

    return if (candidateIndex != -1) this[candidateIndex] else null
}

/**
 * 按范围过滤
 *
 * @param start 起始时间
 * @param end 结束时间
 * @return 匹配的歌词列表
 */
fun <T : ILyricTiming> List<T>.filterByRange(start: Int, end: Int): List<T> {
    if (isEmpty()) return emptyList()
    return filter { it.begin >= start && it.end <= end }
}