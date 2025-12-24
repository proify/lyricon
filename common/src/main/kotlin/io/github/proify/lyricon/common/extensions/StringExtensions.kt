@file:Suppress("unused")

package io.github.proify.lyricon.common.extensions

import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.zip.Deflater
import java.util.zip.Inflater
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.min

/**
 * 将 Float 格式化为字符串，自动处理整数和小数
 */
fun Float.formatToString(maxDecimal: Int = 2): String {
    // 处理整数情况
    if (this % 1 == 0f) return toLong().toString()

    val bigDecimal = BigDecimal.valueOf(toDouble())
    val scale = min(bigDecimal.scale(), maxDecimal)

    return bigDecimal.setScale(scale, RoundingMode.DOWN)
        .stripTrailingZeros()
        .toPlainString()
}

/**
 * 将 Double 格式化为字符串，自动处理整数和小数
 */
fun Double.formatToString(maxDecimal: Int = 2): String {
    // 处理整数情况
    if (this % 1 == 0.0) return toLong().toString()

    val bigDecimal = BigDecimal.valueOf(this)
    val scale = min(bigDecimal.scale(), maxDecimal)

    return bigDecimal.setScale(scale, RoundingMode.DOWN)
        .stripTrailingZeros()
        .toPlainString()
}

/**
 * 压缩字符串为字节数组
 */
fun String.deflate(): ByteArray {
    val defeater = Deflater()
    return try {
        defeater.setInput(toByteArray(Charsets.UTF_8))
        defeater.finish()

        ByteArrayOutputStream().use { outputStream ->
            val buffer = ByteArray(1024)
            while (!defeater.finished()) {
                val count = defeater.deflate(buffer)
                outputStream.write(buffer, 0, count)
            }
            outputStream.toByteArray()
        }
    } finally {
        defeater.end()
    }
}

/**
 * 解压缩字节数组为字符串
 */
fun ByteArray.inflate(): String {
    val inflater = Inflater()
    return try {
        inflater.setInput(this)

        ByteArrayOutputStream().use { outputStream ->
            val buffer = ByteArray(1024)
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                outputStream.write(buffer, 0, count)
            }
            outputStream.toString(Charsets.UTF_8.name())
        }
    } finally {
        inflater.end()
    }
}

// ==================== Base64 编码/解码 ====================

/**
 * Base64 编码字符串
 */
@OptIn(ExperimentalEncodingApi::class)
fun String.base64EncodeToString(): String = Base64.encode(toByteArray())

/**
 * Base64 编码字符串为字节数组
 */
@OptIn(ExperimentalEncodingApi::class)
fun String.base64EncodeToByteArray(): ByteArray = Base64.encodeToByteArray(toByteArray())

/**
 * Base64 解码字符串
 */
@OptIn(ExperimentalEncodingApi::class)
fun String.base64DecodeToString(): String = Base64.decode(toByteArray()).toString(Charsets.UTF_8)

/**
 * Base64 解码字符串为字节数组
 */
@OptIn(ExperimentalEncodingApi::class)
fun String.base64DecodeToByteArray(): ByteArray = Base64.decode(toByteArray())

/**
 * Base64 编码字节数组为字符串
 */
@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.base64EncodeToString(): String = Base64.encode(this)

/**
 * Base64 编码字节数组
 */
@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.base64EncodeToByteArray(): ByteArray = Base64.encodeToByteArray(this)

/**
 * Base64 解码字节数组为字符串
 */
@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.base64DecodeToString(): String = Base64.decode(this).toString(Charsets.UTF_8)

/**
 * Base64 解码字节数组
 */
@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.base64DecodeToByteArray(): ByteArray = Base64.decode(this)