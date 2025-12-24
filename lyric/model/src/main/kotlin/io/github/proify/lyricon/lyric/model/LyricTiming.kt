package io.github.proify.lyricon.lyric.model

/**
 * 表示一段歌词的时间信息。
 *
 * @property begin 起始时间（毫秒）
 * @property end 结束时间（毫秒）
 * @property duration 持续时长（毫秒）
 */
interface LyricTiming {
    var begin: Int
    var end: Int
    var duration: Int

    /** 返回当前对象的深拷贝。 */
    fun deepCopy(): LyricTiming
}

/**
 * 对可变列表执行深拷贝。
 *
 * 每个元素都会调用自身的 [LyricTiming.deepCopy]，并返回全新的 MutableList，不会影响原列表。
 */
@Suppress("UNCHECKED_CAST")
fun <T : LyricTiming> List<T>.deepCopy(): MutableList<T> =
    map { it.deepCopy() as T }.toMutableList()

/**
 * 根据播放位置筛选当前时间段内的所有歌词。
 *
 * @param position 播放位置（毫秒）
 * @return 所有满足 `begin <= position <= end` 的时间段
 */
fun <T : LyricTiming> List<T>.filterByPosition(position: Int): List<T> =
    filter { position in it.begin..it.end }

/**
 * 根据播放位置筛选歌词时间段：
 *
 * - 若有时间段包含当前 position，则返回所有匹配项；
 * - 若无，则返回结束时间在 position 之前的最后一条时间段；
 * - 若仍无，则返回空列表。
 *
 * 常用于歌词逐行显示逻辑：优先显示当前行，若无则回退到上一行。
 *
 * @param position 播放位置（毫秒）
 * @return 当前匹配项或最近的前一项（最多 1 个）
 */
fun <T : LyricTiming> List<T>.filterByPositionOrPrevious(position: Int): List<T> =
    filterByPosition(position).ifEmpty { listOfNotNull(filter { it.end < position }.maxByOrNull { it.end }) }

/**
 * 筛选完全落在指定时间区间内的歌词时间段。
 *
 * 条件：
 * ```
 * begin >= start && end <= end
 * ```
 *
 * @param start 区间起始时间（毫秒）
 * @param end 区间结束时间（毫秒）
 * @return 所有完全位于区间内的时间段
 */
fun <T : LyricTiming> List<T>.filterByRange(start: Int, end: Int): List<T> =
    filter { it.begin >= start && it.end <= end }