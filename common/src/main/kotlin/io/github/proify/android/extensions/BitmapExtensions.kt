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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.core.graphics.createBitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

private const val TAG = "BitmapExtensions"

/**
 * 将当前 [Bitmap] 裁剪为圆角矩形。
 *
 * 使用 [BitmapShader] 实现，相比 Xfermode 方案具有更好的绘制性能。
 *
 * ```kotlin
 * val rounded = bitmap.toRoundedCorner(20f)
 * ```
 *
 * @param cornerRadius 圆角半径（单位：像素）。
 * @param recycleSource 处理完成后是否回收源位图。默认为 `true`。
 * @return 裁剪后的圆角 [Bitmap]。如果失败（如 OOM），则返回原位图。
 */
fun Bitmap.toRoundedCorner(
    @FloatRange(from = 0.0) cornerRadius: Float,
    recycleSource: Boolean = true
): Bitmap {
    if (isRecycled) return this

    return try {
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader =
                BitmapShader(this@toRoundedCorner, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)

        if (recycleSource && output != this) {
            this.recycle()
        }
        output
    } catch (e: OutOfMemoryError) {
        Log.e(TAG, "toRoundedCorner failed due to OOM", e)
        this
    }
}

/**
 * 将 [Bitmap] 保存到指定的本地文件路径。
 *
 * ```kotlin
 * val isSaved = bitmap.saveBitmapToDisk("/path/to/image.png")
 * ```
 *
 * @param path 存储文件的绝对路径。
 * @param format 压缩格式，支持 PNG, JPEG, WEBP。默认为 PNG。
 * @param quality 压缩质量，范围 0-100。PNG 等无损格式会忽略此值。
 * @param recycleBitmap 保存完成后是否回收 [Bitmap] 对象。默认为 `true`。
 * @return 如果保存成功则返回 `true`。
 */
fun Bitmap.saveBitmapToDisk(
    path: String,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    @IntRange(from = 0, to = 100) quality: Int = 100,
    recycleBitmap: Boolean = true
): Boolean {
    if (isRecycled || path.isBlank()) return false

    return try {
        val file = File(path)
        file.parentFile?.let { if (!it.exists()) it.mkdirs() }

        FileOutputStream(file).buffered().use { out ->
            this.compress(format, quality, out)
        }
        true
    } catch (e: Exception) {
        Log.e(TAG, "saveBitmapToDisk failed: ${e.message}", e)
        false
    } finally {
        if (recycleBitmap && !isRecycled) {
            this.recycle()
        }
    }
}

/**
 * 将 [Bitmap] 转换为字节数组。
 *
 * ```kotlin
 * val data = bitmap.toByteArray(Bitmap.CompressFormat.JPEG, 80)
 * ```
 *
 * @param format 压缩格式。
 * @param quality 压缩质量 (0-100)。
 * @param recycleBitmap 转换后是否回收位图。默认为 `false`。
 */
fun Bitmap.toByteArray(
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    @IntRange(from = 0, to = 100) quality: Int = 100,
    recycleBitmap: Boolean = false
): ByteArray {
    if (isRecycled) return byteArrayOf()
    return ByteArrayOutputStream().use { stream ->
        this.compress(format, quality, stream)
        if (recycleBitmap) this.recycle()
        stream.toByteArray()
    }
}

// --- File 扩展 ---

/**
 * 从文件加载位图。
 *
 * ```kotlin
 * val bitmap = File("/path/to/img").toBitmap()
 * ```
 */
fun File.toBitmap(options: BitmapFactory.Options? = null): Bitmap? {
    if (!exists() || !canRead()) return null
    return runCatching {
        BitmapFactory.decodeFile(absolutePath, options)
    }.onFailure {
        Log.e(TAG, "File.toBitmap failed", it)
    }.getOrNull()
}

// --- Drawable 扩展 ---

/**
 * 将 [Drawable] 转换为 [Bitmap]。
 *
 * 如果是 [BitmapDrawable] 则直接返回原位图，否则在新的 Canvas 上绘制。
 *
 * ```kotlin
 * val bitmap = drawable.toBitmap()
 * ```
 */
fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable && bitmap != null) {
        return bitmap
    }
    val w = if (intrinsicWidth <= 0) 1 else intrinsicWidth
    val h = if (intrinsicHeight <= 0) 1 else intrinsicHeight

    return createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
        val canvas = Canvas(this)
        setBounds(0, 0, w, h)
        draw(canvas)
    }
}