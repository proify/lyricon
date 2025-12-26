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
 * @property begin 行开始时间
 * @property end 行结束时间
 * @property duration 持续时间
 * @property text 主歌词文本
 * @property words 主歌词逐词时间信息
 * @property isAlignedRight 歌词显示在右边
 * @property secondaryText 副歌词文本
 * @property secondaryWords 副歌词逐词时间信息
 */
@Serializable
@Parcelize
@TypeParceler<DoubleLyricLine, DoubleLyricLine.ParcelerImpl>()
data class DoubleLyricLine(
    @param:IntRange(0) override var begin: Int = 0,
    @param:IntRange(0) override var end: Int = 0,
    @param:IntRange(0) override var duration: Int = 0,
    var isAlignedRight: Boolean = false,
    var extraMetadata: Map<String, String?>? = null,
    var text: String? = null,
    var words: List<LyricWord>? = null,
    var secondaryText: String? = null,
    var secondaryWords: List<LyricWord>? = null,
) : LyricTiming, Parcelable {

    object ParcelerImpl : Parceler<DoubleLyricLine> {
        private const val PARCEL_VERSION_V1 = 1

        override fun DoubleLyricLine.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(PARCEL_VERSION_V1)
            parcel.writeInt(begin)
            parcel.writeInt(end)
            parcel.writeInt(duration)
            ParcelCompat.writeBoolean(parcel, isAlignedRight)
            parcel.writeMetadata(extraMetadata)
            parcel.writeString(text)
            parcel.writeWordsList(words, flags)
            parcel.writeString(secondaryText)
            parcel.writeWordsList(secondaryWords, flags)
        }

        override fun create(parcel: Parcel): DoubleLyricLine {
            val version = parcel.readInt()
            return when (version) {
                PARCEL_VERSION_V1 -> parcel.readFromV1()
                else -> throw IllegalArgumentException("Unknown parcel version: $version")
            }
        }

        private fun Parcel.readFromV1(): DoubleLyricLine {
            val begin = readInt()
            val end = readInt()
            val duration = readInt()
            val isAlignedRight = ParcelCompat.readBoolean(this)
            val extraMetadata = readMetadata()
            val text = readString()
            val words = readWordsList()
            val secondaryText = readString()
            val secondaryWords = readWordsList()

            return DoubleLyricLine(
                begin = begin,
                end = end,
                duration = duration,
                isAlignedRight = isAlignedRight,
                extraMetadata = extraMetadata,
                text = text,
                words = words,
                secondaryText = secondaryText,
                secondaryWords = secondaryWords
            )
        }
    }

}