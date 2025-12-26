package io.github.proify.lyricon.lyric.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.IntRange
import androidx.core.os.ParcelCompat
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.Serializable

/**
 * @property begin 歌词开始时间
 * @property end 歌词结束时间
 * @property duration 歌词持续时间
 * @property text 歌词文本内容
 * @property words 歌词中每个单词的时间信息列表，用于逐词高亮显示
 * @property direction 歌词显示方向。
 */
@Serializable
@Parcelize
@TypeParceler<LyricLine, LyricLine.ParcelerImpl>()
data class LyricLine(
    @param:IntRange(0) override var begin: Int = 0,
    @param:IntRange(0) override var end: Int = 0,
    @param:IntRange(0) override var duration: Int = 0,
    var isAlignedRight: Boolean = false,
    var extraMetadata: Map<String, String?>? = null,
    var text: String? = null,
    var words: List<LyricWord>? = null,
) : LyricTiming, Parcelable {

    object ParcelerImpl : Parceler<LyricLine> {
        private const val PARCEL_VERSION_V1 = 1

        override fun LyricLine.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(PARCEL_VERSION_V1)
            parcel.writeInt(begin)
            parcel.writeInt(end)
            parcel.writeInt(duration)
            ParcelCompat.writeBoolean(parcel, isAlignedRight)
            parcel.writeMetadata(extraMetadata)
            parcel.writeString(text)
            parcel.writeWordsList(words, flags)
        }

        override fun create(parcel: Parcel): LyricLine {
            return when (parcel.readInt()) {
                PARCEL_VERSION_V1 -> parcel.readFromV1()
                else -> throw IllegalArgumentException("Unknown parcel version")
            }
        }

        private fun Parcel.readFromV1(): LyricLine {
            val begin = readInt()
            val end = readInt()
            val duration = readInt()
            val isAlignedRight = ParcelCompat.readBoolean(this)
            val extraMetadata = readMetadata()
            val text = readString()
            val words = readWordsList()

            return LyricLine(
                begin = begin,
                end = end,
                duration = duration,
                isAlignedRight = isAlignedRight,
                extraMetadata = extraMetadata,
                text = text,
                words = words,
            )
        }
    }
}