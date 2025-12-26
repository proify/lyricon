package io.github.proify.lyricon.lyric.model

import android.os.Parcel
import android.util.ArrayMap
import io.github.proify.lyricon.lyric.model.LyricWord.ParcelerImpl.write

internal fun Parcel.writeMetadata(metadata: Map<String, String?>?) {
    if (metadata == null) {
        writeInt(-1)
        return
    }

    writeInt(metadata.size)
    metadata.forEach { (key, value) ->
        writeString(key)
        writeString(value)
    }
}

internal fun Parcel.readMetadata(): Map<String, String?>? {
    val size = readInt()
    return when {
        size < 0 -> null
        size == 0 -> emptyMap()
        else -> ArrayMap<String, String?>(size).apply {
            repeat(size) {
                val key = readString() ?: ""
                val value = readString()
                put(key, value)
            }
        }
    }
}

internal fun Parcel.writeWordsList(
    words: List<LyricWord>?,
    flags: Int
) {
    if (words == null) {
        writeInt(-1)
        return
    }
    writeInt(words.size)
    words.forEach { word ->
        word.write(this, flags)
    }
}

internal fun Parcel.readWordsList(): List<LyricWord>? {
    val size = readInt()
    return when {
        size < 0 -> null
        size == 0 -> emptyList()
        else -> ArrayList<LyricWord>(size).apply {
            repeat(size) {
                add(LyricWord.ParcelerImpl.create(this@readWordsList))
            }
        }
    }
}

/**
 * 过滤出当前位置正在播放的歌词元素。
 *  @param position 当前播放位置（时间戳）。
 * @return 包含在 [position] 时间范围内的所有元素列表。
 */
fun <T : LyricTiming> List<T>.filterByPosition(position: Int): List<T> =
    filter { position in it.begin..it.end }

/**
 * 过滤出当前位置正在播放的元素，如果没有正在播放的，则返回上一个已结束的元素。
 * 常用于在歌词间隙仍显示最后一句歌词。
 * @param position 当前播放位置（时间戳）。
 * @return 匹配的元素列表。
 */
fun <T : LyricTiming> List<T>.filterByPositionOrPrevious(position: Int): List<T> {
    val current = filterByPosition(position)
    if (current.isNotEmpty()) return current

    var lastItem: T? = null
    for (item in this) {
        if (item.end < position) {
            if (lastItem == null || item.end > lastItem.end) {
                lastItem = item
            }
        }
    }
    return if (lastItem != null) listOf(lastItem) else emptyList()
}

/**
 * 过滤出完全落在指定时间范围内的元素。
 * @param start 起始时间戳（包含）。
 * @param end 结束时间戳（包含）。
 * @return 在该范围内的元素列表。
 */
fun <T : LyricTiming> List<T>.filterByRange(start: Int, end: Int): List<T> =
    filter { it.begin >= start && it.end <= end }