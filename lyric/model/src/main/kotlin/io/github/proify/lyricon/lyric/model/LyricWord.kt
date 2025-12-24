package io.github.proify.lyricon.lyric.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.Serializable

/**
 * 表示歌词中的一个单词的时间片段。
 *
 * 用于逐字/逐词高亮显示。
 * 每个词拥有自己的开始、结束时间与文本内容。
 *
 * @property begin 开始时间（毫秒）
 * @property end 结束时间（毫秒）
 * @property duration 持续时间（毫秒）
 * @property text 词文本内容
 */
@Serializable
@Parcelize
@TypeParceler<LyricWord, LyricWord.ParcelerImpl>()
open class LyricWord(
    override var begin: Int = 0,
    override var end: Int = 0,
    override var duration: Int = 0,
    var text: String? = null
) : LyricTiming, Parcelable {

    constructor(source: LyricWord) : this(
        begin = source.begin,
        end = source.end,
        duration = source.duration,
        text = source.text
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LyricWord) return false

        return begin == other.begin
                && end == other.end
                && duration == other.duration
                && text == other.text
    }

    override fun hashCode(): Int {
        var result = begin
        result = 31 * result + end
        result = 31 * result + duration
        result = 31 * result + (text?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "LyricWord(begin=$begin" +
                ", end=$end" +
                ", duration=$duration" +
                ", text=$text)"
    }

    override fun deepCopy(): LyricWord = LyricWord(this)

    object ParcelerImpl : Parceler<LyricWord> {

        private const val PARCEL_VERSION_V1 = 1
        private const val PARCEL_VERSION = PARCEL_VERSION_V1

        override fun LyricWord.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(PARCEL_VERSION)
            parcel.writeInt(begin)
            parcel.writeInt(end)
            parcel.writeInt(duration)
            parcel.writeString(text)
        }

        override fun create(parcel: Parcel): LyricWord {
            val version = parcel.readInt()
            return when (version) {
                PARCEL_VERSION_V1 -> {
                    val begin = parcel.readInt()
                    val end = parcel.readInt()
                    val duration = parcel.readInt()
                    val text = parcel.readString()
                    LyricWord(begin, end, duration, text)
                }

                else -> throw IllegalArgumentException("Unsupported parcel version: $version")
            }
        }
    }
}