package com.makino.lyricon.model.lyric.provider.base

import com.makino.lyricon.model.lyric.provider.ILyricPositionProvider
import io.github.proify.lyricon.lyric.model.interfaces.ILyricTiming

class Base(val source: List<ILyricTiming>) : ILyricPositionProvider {

    override fun testfind(position: Long, action: (ILyricTiming) -> Unit): Int {
        if (source.isEmpty()) return 0

        // 二分查找第一个可能匹配的区间
        var left = 0
        var right = source.size - 1
        var foundIndex = -1

        while (left <= right) {
            val mid = (left + right) / 2
            val item = source[mid]

            when {
                position < item.begin -> right = mid - 1
                position >= item.end -> left = mid + 1
                else -> {
                    foundIndex = mid
                    break
                }
            }
        }

        if (foundIndex == -1) return 0

        var count = 0

        // 向前扫描重叠区间
        var i = foundIndex
        while (i >= 0 && position >= source[i].begin && position < source[i].end) {
            action(source[i])
            count++
            i--
        }

        // 向后扫描重叠区间（注意跳过 foundIndex 本身）
        i = foundIndex + 1
        while (i < source.size && position >= source[i].begin && position < source[i].end) {
            action(source[i])
            count++
            i++
        }

        return count
    }
}
