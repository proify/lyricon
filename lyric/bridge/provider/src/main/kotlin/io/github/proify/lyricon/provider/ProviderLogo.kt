/*
 * Copyright 2026 Proify
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.proify.lyricon.provider

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.annotation.IntDef
import androidx.core.graphics.drawable.toBitmap
import io.github.proify.lyricon.provider.ProviderLogo.Companion.TYPE_BITMAP
import io.github.proify.lyricon.provider.ProviderLogo.Companion.TYPE_SVG
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.Serializable
import java.io.ByteArrayOutputStream
import kotlin.io.encoding.Base64

/**
 * @property data Logo 原始字节数据
 * @property type Logo 类型，取值见 [TYPE_BITMAP]、[TYPE_SVG]
 */
@Serializable
@Parcelize
data class ProviderLogo(
    val data: ByteArray,
    @param:Type @property:Type val type: Int
) : Parcelable {

    /**
     * 将数据解析为 [Bitmap]。
     */
    fun toBitmap(): Bitmap? = if (type == TYPE_BITMAP) {
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
     */
    fun toSvg(): String? = if (type == TYPE_SVG) data.toString(Charsets.UTF_8) else null

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(TYPE_BITMAP, TYPE_SVG)
    annotation class Type

    companion object {
        const val TYPE_BITMAP = 0
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
            ProviderLogo(svg.toByteArray(Charsets.UTF_8), TYPE_SVG)

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