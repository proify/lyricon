package io.github.proify.lyricon.common.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.FloatRange
import androidx.core.graphics.createBitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

object CommonUtils {

    /**
     * 创建圆角位图
     * @param bitmap 源位图
     * @param cornerRadius 圆角半径
     * @param recycleSource 是否回收源位图,默认true
     * @return 圆角位图
     */
    fun getRoundedCornerBitmap(
        bitmap: Bitmap,
        cornerRadius: Float,
        recycleSource: Boolean = true
    ): Bitmap {
        val output = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height,
            Bitmap.Config.ARGB_8888
        )

        Canvas(output).apply {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
            }

            val rectF = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())

            drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            drawBitmap(bitmap, 0f, 0f, paint)
        }

        if (recycleSource && !bitmap.isRecycled) {
            bitmap.recycle()
        }

        return output
    }

    /**
     * 保存位图到磁盘
     * @param bitmap 位图
     * @param path 保存路径
     * @param format 压缩格式,默认PNG
     * @param quality 压缩质量(0-100),默认100
     * @param recycleBitmap 是否回收位图,默认true
     * @return 是否保存成功
     */
    fun saveBitmapToDisk(
        bitmap: Bitmap?,
        path: String?,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100,
        recycleBitmap: Boolean = true
    ): Boolean {
        if (bitmap == null || bitmap.isRecycled || path.isNullOrBlank()) {
            return false
        }

        return try {
            val file = File(path)

            // 确保父目录存在
            file.parentFile?.mkdirs()

            // 如果文件已存在则删除
            if (file.exists()) {
                file.delete()
            }

            FileOutputStream(file).use { out ->
                bitmap.compress(format, quality, out)
                out.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            if (recycleBitmap && !bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }

    /**
     * 从文件加载位图
     * @param file 文件对象
     * @param options BitmapFactory选项,可用于控制采样率等
     * @return 位图或null
     */
    fun fileToBitmap(file: File, options: BitmapFactory.Options? = null): Bitmap? {
        if (!file.exists() || !file.canRead()) {
            return null
        }

        return try {
            if (options != null) {
                BitmapFactory.decodeFile(file.absolutePath, options)
            } else {
                BitmapFactory.decodeFile(file.absolutePath)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 位图转字节数组
     * @param bitmap 位图
     * @param format 压缩格式,默认PNG
     * @param quality 压缩质量(0-100),默认100
     * @param recycleBitmap 是否回收位图,默认true
     * @return 字节数组
     */
    fun bitmapToByteArray(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100,
        recycleBitmap: Boolean = true
    ): ByteArray {
        val stream = ByteArrayOutputStream()
        return try {
            bitmap.compress(format, quality, stream)
            stream.toByteArray()
        } finally {
            stream.close()
            if (recycleBitmap && !bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }

    /**
     * Drawable转位图
     * @param drawable Drawable对象
     * @return 位图
     */
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        // 如果已经是BitmapDrawable,直接返回其位图
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }

        // 处理固有宽高
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 1
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 1

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)

        return bitmap
    }

    /**
     * 设置颜色的透明度分量
     * @param color 原始颜色
     * @param alpha 透明度(0.0-1.0)
     * @return 新颜色值
     */
    fun setAlphaComponent(
        color: Int,
        @FloatRange(from = 0.0, to = 1.0) alpha: Float
    ): Int {
        val clampedAlpha = alpha.coerceIn(0f, 1f)
        val alphaInt = (clampedAlpha * 255f + 0.5f).roundToInt()
        return (color and 0x00FFFFFF) or (alphaInt shl 24)
    }
}