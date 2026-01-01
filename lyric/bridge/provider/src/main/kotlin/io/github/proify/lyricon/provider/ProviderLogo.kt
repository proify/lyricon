/*
 * Lyricon – An Xposed module that extends system functionality
 * Copyright (C) 2026 Proify
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.proify.lyricon.provider

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.IntDef
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import io.github.proify.lyricon.provider.ProviderLogo.Companion.TYPE_BITMAP
import io.github.proify.lyricon.provider.ProviderLogo.Companion.TYPE_SVG
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import kotlin.io.encoding.Base64

/**
 * 提供商 Logo。
 *
 * 支持 Bitmap（二进制 PNG）与 SVG（UTF-8 文本）两种表示形式，
 *
 * @property data Logo 原始字节数据
 * @property type Logo 类型，取值见 [TYPE_BITMAP]、[TYPE_SVG]
 */
@Parcelize
@TypeParceler<ProviderLogo, ProviderLogo.ParcelerImpl>()
data class ProviderLogo(
    val data: ByteArray,
    @param:Type @property:Type val type: Int
) : Parcelable {

    init {
        require(type == TYPE_BITMAP || type == TYPE_SVG) {
            "Invalid type: $type"
        }
        require(data.size <= MAX_SIZE) {
            "ProviderLogo data exceeds max size: ${data.size} > $MAX_SIZE"
        }
    }

    /**
     * 将数据解析为 [Bitmap]。
     *
     * 仅在类型为 [TYPE_BITMAP] 时有效。
     */
    fun toBitmap(): Bitmap? =
        if (type == TYPE_BITMAP) {
            runCatching {
                BitmapFactory.decodeByteArray(
                    data,
                    0,
                    data.size,
                    BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                )
            }.getOrNull()
        } else {
            null
        }

    /**
     * 将数据解析为 SVG 字符串。
     *
     * 仅在类型为 [TYPE_SVG] 时有效。
     */
    fun toSvg(): String? =
        if (type == TYPE_SVG) String(data, StandardCharsets.UTF_8) else null

    override fun toString(): String =
        "ProviderLogo(type=${typeToString(type)}, size=${data.size})"

    /**
     * Logo 类型限定。
     */
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(TYPE_BITMAP, TYPE_SVG)
    annotation class Type

    object ParcelerImpl : Parceler<ProviderLogo> {

        override fun ProviderLogo.write(parcel: Parcel, flags: Int) {
            parcel.writeBundle(
                bundleOf(
                    "data" to data,
                    "type" to type
                )
            )
        }

        override fun create(parcel: Parcel): ProviderLogo {
            val bundle = parcel.readBundle(ProviderLogo::class.java.classLoader)
            return ProviderLogo(
                data = bundle?.getByteArray("data") ?: ByteArray(0),
                type = bundle?.getInt("type", TYPE_BITMAP) ?: TYPE_BITMAP,
            )
        }
    }

    companion object {

        /** Logo 数据最大允许大小（256 KB） */
        const val MAX_SIZE = 256 * 1024

        /** Bitmap（PNG）类型 */
        const val TYPE_BITMAP = 0

        /** SVG 文本类型 */
        const val TYPE_SVG = 1

        /**
         * 由 [Bitmap] 构建 Logo。
         *
         * @param bitmap 源 Bitmap
         * @param recycle 是否回收源 Bitmap
         */
        fun fromBitmap(bitmap: Bitmap, recycle: Boolean = true): ProviderLogo =
            ProviderLogo(bitmap.toPngBytes(recycle), TYPE_BITMAP)

        /**
         * 由 [Drawable] 构建 Logo。
         */
        fun fromDrawable(drawable: Drawable): ProviderLogo =
            fromBitmap(drawable.toBitmap())

        /**
         * 由 SVG 文本构建 Logo。
         */
        fun fromSvg(svg: String): ProviderLogo =
            ProviderLogo(svg.toByteArray(StandardCharsets.UTF_8), TYPE_SVG)

        /**
         * 由 Base64 编码的 PNG 数据构建 Logo。
         */
        fun fromBase64(base64: String): ProviderLogo =
            ProviderLogo(Base64.Default.decode(base64), TYPE_BITMAP)

        private fun Bitmap.toPngBytes(recycle: Boolean): ByteArray =
            ByteArrayOutputStream().use { out ->
                compress(Bitmap.CompressFormat.PNG, 100, out)
                out.toByteArray()
            }.also { if (recycle) recycle() }

        private fun typeToString(type: Int): String =
            when (type) {
                TYPE_BITMAP -> "BITMAP"
                TYPE_SVG -> "SVG"
                else -> "UNKNOWN"
            }
    }
}