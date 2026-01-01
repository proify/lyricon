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

package io.github.proify.android.extensions

import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.zip.Deflater
import java.util.zip.Inflater
import kotlin.io.encoding.Base64
import kotlin.math.min

/**
 * 将 [Float] 格式化为易读的字符串。
 *
 * - 如果是整数，则直接返回整数部分（如 1.0 -> "1"）。
 * - 如果是小数，则保留至多 [maxDecimal] 位，并去除末尾多余的零。
 * - 自动处理 [Float.NaN] 和 [Float.POSITIVE_INFINITY] 等特殊情况。
 *
 * @param maxDecimal 最大保留小数位数，默认为 2。
 * @param roundingMode 舍入模式，默认为 [RoundingMode.DOWN]（直接截断）。
 * @return 格式化后的字符串。
 */
fun Float.formatToString(
    maxDecimal: Int = 2,
    roundingMode: RoundingMode = RoundingMode.DOWN
): String {
    if (isNaN()) return "NaN"
    if (isInfinite()) return if (this > 0) "Infinity" else "-Infinity"
    return toDouble().formatToString(maxDecimal, roundingMode)
}

/**
 * 将 [Double] 格式化为易读的字符串。
 *
 * - 如果是整数，则直接返回整数部分（如 1.0 -> "1"）。
 * - 如果是小数，则保留至多 [maxDecimal] 位，并去除末尾多余的零。
 *
 * @param maxDecimal 最大保留小数位数，默认为 2。
 * @param roundingMode 舍入模式，默认为 [RoundingMode.DOWN]。
 * @return 格式化后的字符串。
 */
fun Double.formatToString(
    maxDecimal: Int = 2,
    roundingMode: RoundingMode = RoundingMode.DOWN
): String {
    if (isNaN()) return "NaN"
    if (isInfinite()) return if (this > 0) "Infinity" else "-Infinity"

    // 处理整数情况，避免 BigDecimal 开销
    if (this % 1 == 0.0) return toLong().toString()

    return BigDecimal.valueOf(this).let {
        val scale = min(it.scale(), maxDecimal)
        it.setScale(scale, roundingMode)
            .stripTrailingZeros()
            .toPlainString()
    }
}

/**
 * 使用 ZLIB 算法压缩字符串。
 *
 * @return 压缩后的字节数组。
 */
fun String.deflate(): ByteArray {
    if (isEmpty()) return byteArrayOf()
    val deflater = Deflater()
    return try {
        deflater.setInput(toByteArray(Charsets.UTF_8))
        deflater.finish()

        ByteArrayOutputStream().use { output ->
            val buffer = ByteArray(1024)
            while (!deflater.finished()) {
                val count = deflater.deflate(buffer)
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }
    } finally {
        deflater.end()
    }
}

/**
 * 使用 ZLIB 算法解压缩字节数组。
 *
 * @return 解压后的字符串。
 * @throws java.util.zip.DataFormatException 如果数据格式非法。
 */
fun ByteArray.inflate(): String {
    if (isEmpty()) return ""
    val inflater = Inflater()
    return try {
        inflater.setInput(this)

        ByteArrayOutputStream().use { output ->
            val buffer = ByteArray(1024)
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                if (count == 0 && inflater.needsInput()) break
                output.write(buffer, 0, count)
            }
            output.toString(Charsets.UTF_8.name())
        }
    } finally {
        inflater.end()
    }
}

// ==================== Base64 编码/解码 ====================

/**
 * 将字符串进行 Base64 编码。
 */
fun String.base64EncodeToString(): String = Base64.encode(this.toByteArray())

/**
 * 将字符串编码为 Base64 格式的字节数组。
 */
fun String.base64EncodeToByteArray(): ByteArray = Base64.encodeToByteArray(this.toByteArray())

/**
 * 将 Base64 编码的字符串解码为原始字符串。
 *
 * @return 解码后的字符串，使用 UTF-8 编码。
 */
fun String.base64DecodeToString(): String = Base64.decode(this).toString(Charsets.UTF_8)

/**
 * 将 Base64 编码的字符串解码为字节数组。
 */
fun String.base64DecodeToByteArray(): ByteArray = Base64.decode(this)

/**
 * 将字节数组进行 Base64 编码，返回编码后的字符串。
 */
fun ByteArray.base64EncodeToString(): String = Base64.encode(this)

/**
 * 将字节数组进行 Base64 编码，返回编码后的字节数组。
 */
fun ByteArray.base64EncodeToByteArray(): ByteArray = Base64.encodeToByteArray(this)

/**
 * 将 Base64 格式的字节数组解码为原始字符串。
 */
fun ByteArray.base64DecodeToString(): String = Base64.decode(this).toString(Charsets.UTF_8)

/**
 * 将 Base64 格式的字节数组解码。
 */
fun ByteArray.base64DecodeToByteArray(): ByteArray = Base64.decode(this)