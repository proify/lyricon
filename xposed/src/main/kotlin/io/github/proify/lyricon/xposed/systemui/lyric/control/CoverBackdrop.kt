/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.lyric.control

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.core.graphics.scale
import io.github.proify.lyricon.xposed.logger.YLog
import java.io.File

/**
 * 封面取色辅助：解码封面、统计占优色并生成暗化背景色。
 *
 * 采用简单可靠的像素统计（缩小采样 + 平均），不依赖 ColorExtractorImpl 的 K-means 算法，
 * 保证在 SystemUI 进程内稳定运行。
 *
 * @author Tomakino
 * @since 2026
 */
internal object CoverBackdrop {

    private const val TAG = "CoverBackdrop"

    /** 采样尺寸（宽=高）。 */
    private const val SAMPLE_SIZE = 24

    /** 忽略低于该透明度的像素。 */
    private const val MIN_ALPHA = 80

    /** 暗化系数：保留色相，略压暗亮度。 */
    private const val DARKEN_FACTOR = 0.5f

    /** 无有效像素时的回退颜色（深靛蓝黑）。 */
    private const val DEFAULT_COLOR = 0xFF20242E.toInt()

    /** 解码封面文件（文件不存在或解码失败返回 null）。 */
    fun decode(file: File): Bitmap? {
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            YLog.error(TAG, "Failed to decode cover " + file.absolutePath, e)
            null
        }
    }

    /** 封面主色暗化后的卡片背景色。 */
    fun backdropColor(bitmap: Bitmap): Int = darken(dominantColor(bitmap), DARKEN_FACTOR)

    /**
     * 像素级占优色统计：缩小到 ~24x24 采样求平均，归一到完整 alpha。
     */
    private fun dominantColor(src: Bitmap): Int {
        return try {
            if (src.width <= 0 || src.height <= 0) return DEFAULT_COLOR

            val small = src.scale(SAMPLE_SIZE, SAMPLE_SIZE)
            val pixels = IntArray(SAMPLE_SIZE * SAMPLE_SIZE)
            small.getPixels(pixels, 0, SAMPLE_SIZE, 0, 0, SAMPLE_SIZE, SAMPLE_SIZE)
            if (small !== src) small.recycle()

            var r = 0L
            var g = 0L
            var b = 0L
            var n = 0L
            for (p in pixels) {
                // 忽略接近透明的像素
                val a = (p shr 24) and 0xFF
                if (a < MIN_ALPHA) continue
                r += (p shr 16) and 0xFF
                g += (p shr 8) and 0xFF
                b += p and 0xFF
                n++
            }
            if (n == 0L) return DEFAULT_COLOR
            Color.rgb(
                (r / n).toInt().coerceIn(0, 255),
                (g / n).toInt().coerceIn(0, 255),
                (b / n).toInt().coerceIn(0, 255)
            )
        } catch (e: Exception) {
            YLog.warning(TAG, "dominantColor failed")
            DEFAULT_COLOR
        }
    }

    /** 把颜色压暗到深色区间（保留色相，仅降低亮度）。 */
    private fun darken(color: Int, factor: Float): Int {
        val r = (Color.red(color) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * factor).toInt().coerceIn(0, 255)
        return Color.argb(255, r, g, b)
    }
}
