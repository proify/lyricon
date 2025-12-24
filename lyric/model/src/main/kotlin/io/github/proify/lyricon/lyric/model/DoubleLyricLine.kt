package io.github.proify.lyricon.lyric.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.IntDef
import androidx.core.os.ParcelCompat
import io.github.proify.lyricon.lyric.model.DoubleLyricLine.Companion.DIRECTION_DEFAULT
import io.github.proify.lyricon.lyric.model.DoubleLyricLine.Companion.DIRECTION_END
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.Serializable

/**
 * 表示一行主副歌词及其时间与词语信息。
 *
 * 提供逐字高亮所需的逐词时间数据，同时包含歌词方向信息。
 *
 * @property begin 行开始时间（毫秒）
 * @property end 行结束时间（毫秒）
 * @property duration 持续时间（毫秒）
 * @property text 主歌词文本
 * @property words 主歌词逐词时间信息
 * @property direction 歌词显示方向，可取值为 [DIRECTION_DEFAULT] 或 [DIRECTION_END]
 * @property secondaryText 副歌词文本，可为空
 * @property secondaryWords 副歌词逐词时间信息，可为空
 */
@Serializable
@Parcelize
@TypeParceler<DoubleLyricLine, DoubleLyricLine.ParcelerImpl>()
open class DoubleLyricLine(
    override var begin: Int = 0,
    override var end: Int = 0,
    override var duration: Int = 0,
    var text: String? = null,
    var words: List<LyricWord> = mutableListOf(),
    @param:Direction @property:Direction var direction: Int = DIRECTION_DEFAULT,
    var secondaryText: String? = null,
    var secondaryWords: List<LyricWord> = mutableListOf(),
) : LyricTiming, Parcelable {

    /**
     * 构造函数，通过文本初始化主歌词
     *
     * @param text 主歌词文本
     */
    constructor(text: String) : this() {
        this.text = text
    }

    /**
     * 拷贝构造函数，深拷贝主副歌词及其逐词信息
     *
     * @param source 源 DoubleLyricLine 对象
     */
    constructor(source: DoubleLyricLine) : this(
        begin = source.begin,
        end = source.end,
        duration = source.duration,
        text = source.text,
        words = source.words.map(LyricWord::deepCopy),
        direction = source.direction,
        secondaryText = source.secondaryText,
        secondaryWords = source.secondaryWords.map(LyricWord::deepCopy).toMutableList()
    )

    override fun deepCopy(): DoubleLyricLine = DoubleLyricLine(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DoubleLyricLine) return false

        return begin == other.begin
                && end == other.end
                && duration == other.duration
                && text == other.text
                && words == other.words
                && direction == other.direction
                && secondaryText == other.secondaryText
                && secondaryWords == other.secondaryWords
    }

    override fun hashCode(): Int {
        var result = begin
        result = 31 * result + end
        result = 31 * result + duration
        result = 31 * result + (text?.hashCode() ?: 0)
        result = 31 * result + words.hashCode()
        result = 31 * result + direction
        result = 31 * result + (secondaryText?.hashCode() ?: 0)
        result = 31 * result + secondaryWords.hashCode()
        return result
    }

    override fun toString(): String {
        return "DoubleLyricLine(begin=$begin" +
                ", end=$end" +
                ", duration=$duration" +
                ", text=$text" +
                ", words=$words" +
                ", direction=$direction" +
                ", secondaryText=$secondaryText" +
                ", secondaryWords=$secondaryWords)"
    }

    companion object {
        /** 默认歌词显示方向 */
        const val DIRECTION_DEFAULT = 0

        /** 末端方向显示 */
        const val DIRECTION_END = 1

        /**
         * 歌词方向注解，用于限定 [direction] 的取值
         */
        @Retention(AnnotationRetention.SOURCE)
        @IntDef(DIRECTION_DEFAULT, DIRECTION_END)
        annotation class Direction
    }

    /**
     * 自定义 Parceler 实现，支持版本控制
     */
    object ParcelerImpl : Parceler<DoubleLyricLine> {

        private const val PARCEL_VERSION_V1 = 1
        private const val PARCEL_VERSION = PARCEL_VERSION_V1

        /**
         * 将 DoubleLyricLine 写入 Parcel
         *
         * @param parcel 写入目标 Parcel
         * @param flags 写入标志
         */
        override fun DoubleLyricLine.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(PARCEL_VERSION)
            parcel.writeInt(begin)
            parcel.writeInt(end)
            parcel.writeInt(duration)
            parcel.writeString(text)
            parcel.writeParcelableList(words, flags)
            parcel.writeInt(direction)
            parcel.writeString(secondaryText)
            parcel.writeParcelableList(secondaryWords, flags)
        }

        /**
         * 从 Parcel 创建 DoubleLyricLine 对象
         *
         * @param parcel 源 Parcel
         * @return 解析得到的 DoubleLyricLine 对象
         * @throws IllegalArgumentException 不支持的 Parcel 版本
         */
        override fun create(parcel: Parcel): DoubleLyricLine {
            val version = parcel.readInt()
            return when (version) {
                PARCEL_VERSION_V1 -> {
                    val begin = parcel.readInt()
                    val end = parcel.readInt()
                    val duration = parcel.readInt()

                    val text = parcel.readString()
                    val words = mutableListOf<LyricWord>()
                    ParcelCompat.readParcelableList(
                        parcel,
                        words,
                        LyricWord::class.java.classLoader,
                        LyricWord::class.java
                    )

                    val direction = parcel.readInt()

                    val secondaryText = parcel.readString()
                    val secondaryWords = mutableListOf<LyricWord>()
                    ParcelCompat.readParcelableList(
                        parcel,
                        secondaryWords,
                        LyricWord::class.java.classLoader,
                        LyricWord::class.java
                    )

                    DoubleLyricLine(
                        begin,
                        end,
                        duration,
                        text,
                        words,
                        direction,
                        secondaryText,
                        secondaryWords
                    )
                }

                else -> throw IllegalArgumentException("Unsupported parcel version: $version")
            }
        }
    }
}