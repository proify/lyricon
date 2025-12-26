package io.github.proify.lyricon.lyric.model

/**
 * @property begin 起始时间
 * @property end 结束时间
 * @property duration 持续时长
 */
interface LyricTiming {
    var begin: Int
    var end: Int
    var duration: Int
}