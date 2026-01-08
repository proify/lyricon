package io.github.proify.lyricon.provider.extensions

import java.io.ByteArrayOutputStream
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

/**
 * 默认缓冲区大小 (8 KB)
 */
private const val DEFAULT_BUFFER_SIZE = 8192

/**
 * 使用 ZLIB 算法压缩字节数组。
 *
 * @param level 压缩等级，默认为 [Deflater.DEFAULT_COMPRESSION]。
 * @param bufferSize 内部处理缓冲区大小。
 * @return 压缩后的字节数组。若输入为空则返回空字节数组。
 */
fun ByteArray.deflate(
    level: Int = Deflater.DEFAULT_COMPRESSION,
    bufferSize: Int = DEFAULT_BUFFER_SIZE
): ByteArray {
    if (isEmpty()) return byteArrayOf()

    val deflater = Deflater(level)
    // 预估输出大小为输入的 50% 或至少 bufferSize，减少扩容开销
    val outputStream = ByteArrayOutputStream(maxOf(bufferSize, size / 2))

    return try {
        deflater.setInput(this)
        deflater.finish()

        val buffer = ByteArray(bufferSize)
        while (!deflater.finished()) {
            val count = deflater.deflate(buffer)
            if (count > 0) {
                outputStream.write(buffer, 0, count)
            }
        }
        outputStream.toByteArray()
    } finally {
        deflater.end()
    }
}

/**
 * 使用 ZLIB 算法解压缩字节数组。
 *
 * @param bufferSize 内部处理缓冲区大小。
 * @return 解压后的字节数组。
 * @throws DataFormatException 如果数据不是有效的 ZLIB 格式。
 * @throws IllegalArgumentException 如果输入为空。
 */
@Throws(DataFormatException::class)
fun ByteArray.inflate(
    bufferSize: Int = DEFAULT_BUFFER_SIZE
): ByteArray {
    if (isEmpty()) return byteArrayOf()

    val inflater = Inflater()
    // 预估解压后的大小为输入的 2 倍，减少扩容开销
    val outputStream = ByteArrayOutputStream(size * 2)

    return try {
        inflater.setInput(this)

        val buffer = ByteArray(bufferSize)
        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            if (count > 0) {
                outputStream.write(buffer, 0, count)
            } else if (inflater.needsInput()) {
                break
            }
        }
        outputStream.toByteArray()
    } finally {
        inflater.end()
    }
}