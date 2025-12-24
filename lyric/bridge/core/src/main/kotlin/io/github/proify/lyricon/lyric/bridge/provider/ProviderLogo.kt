package io.github.proify.lyricon.lyric.bridge.provider

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.IntDef
import androidx.core.graphics.drawable.toBitmap
import io.github.proify.lyricon.lyric.bridge.provider.ProviderLogo.Companion.TYPE_BITMAP
import io.github.proify.lyricon.lyric.bridge.provider.ProviderLogo.Companion.TYPE_SVG
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import kotlin.io.encoding.Base64

/**
 * 提供商 Logo 数据类，可表示为 Bitmap 或 SVG 文本。
 *
 * @property data Logo 原始字节数据
 * @property type Logo 类型，支持 [TYPE_BITMAP], [TYPE_SVG]
 */
@Parcelize
@TypeParceler<ProviderLogo, ProviderLogo.ParcelerImpl>()
data class ProviderLogo(
    val data: ByteArray,
    @param:Type @property:Type val type: Int
) : Parcelable {

    init {
        require(type in setOf(TYPE_BITMAP, TYPE_SVG)) {
            "Invalid type: $type"
        }
        require(data.size <= MAX_SIZE) {
            "ProviderLogo data exceeds max size: ${data.size} > $MAX_SIZE"
        }
    }

    /**
     * 将 Logo 数据转换为 Bitmap。
     *
     * @return Bitmap 对象，如果类型不为 [TYPE_BITMAP] 或解码失败返回 null
     */
    fun toBitmap(): Bitmap? = if (type == TYPE_BITMAP) {
        runCatching {
            BitmapFactory.decodeByteArray(data, 0, data.size, BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            })
        }.getOrNull()
    } else null

    /**
     * 将 Logo 数据转换为 SVG 文本。
     *
     * @return SVG 字符串，如果类型不为 [TYPE_SVG] 返回 null
     */
    fun toSvg(): String? = if (type == TYPE_SVG) String(data, StandardCharsets.UTF_8) else null

    override fun toString(): String = "ProviderLogo(type=${typeToString(type)}, size=${data.size})"

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(TYPE_BITMAP, TYPE_SVG)
    annotation class Type

    object ParcelerImpl : Parceler<ProviderLogo> {

        override fun ProviderLogo.write(parcel: Parcel, flags: Int) {
            val bundle = Bundle().apply {
                putByteArray("data", data)
                putInt("type", type)
            }
            parcel.writeBundle(bundle)
        }

        override fun create(parcel: Parcel): ProviderLogo {
            val bundle = parcel.readBundle(ProviderLogo::class.java.classLoader) ?: Bundle()
            val data = bundle.getByteArray("data") ?: ByteArray(0)
            val type = bundle.getInt("type", 0)
            return ProviderLogo(data, type)
        }
    }

    companion object {

        /** 单个 Logo 最大数据大小（256 KB） */
        const val MAX_SIZE = 256 * 1024

        /** Bitmap 类型 */
        const val TYPE_BITMAP = 0

        /** SVG 文本类型 */
        const val TYPE_SVG = 1

        /**
         * 根据 Bitmap 创建 ProviderLogo
         *
         * @param bitmap Bitmap 对象
         * @param recycle 是否回收原 Bitmap，默认为 true
         */
        fun fromBitmap(bitmap: Bitmap, recycle: Boolean = true): ProviderLogo =
            ProviderLogo(bitmap.toPngBytes(recycle), TYPE_BITMAP)

        /**
         * 根据 Drawable 创建 ProviderLogo
         *
         * @param drawable Drawable 对象
         */
        fun fromDrawable(drawable: Drawable): ProviderLogo = fromBitmap(drawable.toBitmap())

        /**
         * 根据 SVG 文本创建 ProviderLogo
         *
         * @param svg SVG 字符串
         */
        fun fromSvg(svg: String): ProviderLogo =
            ProviderLogo(svg.toByteArray(StandardCharsets.UTF_8), TYPE_SVG)

        /**
         * 根据 Base64 字符串创建 ProviderLogo（默认为 Bitmap 类型）
         *
         * @param base64 Base64 编码字符串
         */
        fun fromBase64(base64: String): ProviderLogo =
            ProviderLogo(Base64.decode(base64), TYPE_BITMAP)

        /**
         * 将 Bitmap 转为 PNG 字节数组
         */
        private fun Bitmap.toPngBytes(recycle: Boolean): ByteArray =
            ByteArrayOutputStream().use { out ->
                compress(Bitmap.CompressFormat.PNG, 100, out)
                out.toByteArray()
            }.also { if (recycle) recycle() }

        private fun typeToString(type: Int): String = when (type) {
            TYPE_BITMAP -> "BITMAP"
            TYPE_SVG -> "SVG"
            else -> "UNKNOWN"
        }
    }
}