package io.github.proify.lyricon.lyric.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.IntDef
import androidx.core.os.ParcelCompat
import io.github.proify.lyricon.lyric.model.LyricLine.Companion.DIRECTION_DEFAULT
import io.github.proify.lyricon.lyric.model.LyricLine.Companion.DIRECTION_END
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.Serializable

/**
 * 表示歌词中的一行及其时间与词语信息。
 *
 * 用于逐字/逐词高亮显示。
 *
 * @property begin 行开始时间（毫秒）
 * @property end 行结束时间（毫秒）
 * @property duration 持续时间（毫秒）
 * @property text 完整歌词文本
 * @property words 歌词逐词时间信息
 * @property direction 歌词显示方向，可取值为 [DIRECTION_DEFAULT] 或 [DIRECTION_END]
 */
@Serializable
@Parcelize
@TypeParceler<LyricLine, LyricLine.ParcelerImpl>()
open class LyricLine(
    override var begin: Int = 0,
    override var end: Int = 0,
    override var duration: Int = 0,
    var text: String? = null,
    var words: MutableList<LyricWord> = mutableListOf(),
    @param:Direction @property:Direction var direction: Int = DIRECTION_DEFAULT
) : LyricTiming, Parcelable {

    /**
     * 构造函数，通过文本初始化歌词行。
     *
     * @param text 歌词文本
     */
    constructor(text: String) : this() {
        this.text = text
    }

    /**
     * 拷贝构造函数，深拷贝词列表。
     *
     * @param source 源 LyricLine 对象
     */
    constructor(source: LyricLine) : this(
        begin = source.begin,
        end = source.end,
        duration = source.duration,
        text = source.text,
        words = source.words.map { it.deepCopy() }.toMutableList(),
        direction = source.direction
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LyricLine) return false

        return begin == other.begin
                && end == other.end
                && duration == other.duration
                && text == other.text
                && words == other.words
                && direction == other.direction
    }

    override fun hashCode(): Int {
        var result = begin
        result = 31 * result + end
        result = 31 * result + duration
        result = 31 * result + (text?.hashCode() ?: 0)
        result = 31 * result + words.hashCode()
        result = 31 * result + direction
        return result
    }

    override fun toString(): String {
        return "LyricLine(begin=$begin" +
                ", end=$end" +
                ", duration=$duration" +
                ", text=$text" +
                ", words=$words" +
                ", direction=$direction)"
    }

    override fun deepCopy(): LyricLine = LyricLine(this)

    companion object {
        /** 默认歌词方向 */
        const val DIRECTION_DEFAULT = 0

        /** 末端方向显示 */
        const val DIRECTION_END = 1

        /**
         * 歌词方向注解，用于限定 [direction] 可取值
         */
        @Retention(AnnotationRetention.SOURCE)
        @IntDef(DIRECTION_DEFAULT, DIRECTION_END)
        annotation class Direction
    }

    /**
     * 自定义 Parceler 实现，用于性能敏感场景并支持版本控制。
     *
     * 使用 [ParcelCompat.readParcelableList] 安全读取 [words] 列表。
     */
    object ParcelerImpl : Parceler<LyricLine> {

        private const val PARCEL_VERSION_V1 = 1
        private const val PARCEL_VERSION = PARCEL_VERSION_V1

        override fun LyricLine.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(PARCEL_VERSION)
            parcel.writeInt(begin)
            parcel.writeInt(end)
            parcel.writeInt(duration)
            parcel.writeString(text)
            parcel.writeParcelableList(words, flags)
            parcel.writeInt(direction)
        }

        override fun create(parcel: Parcel): LyricLine {
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
                    LyricLine(begin, end, duration, text, words, direction)
                }

                else -> throw IllegalArgumentException("Unsupported parcel version: $version")
            }
        }
    }
}