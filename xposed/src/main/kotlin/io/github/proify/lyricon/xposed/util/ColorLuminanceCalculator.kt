package io.github.proify.lyricon.xposed.util

import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import kotlin.math.pow

object ColorLuminanceCalculator {

    private const val LIGHT_COLOR_THRESHOLD = 0.65

    // sRGB 转线性 RGB 的阈值
    private const val SRGB_TO_LINEAR_THRESHOLD = 0.04045
    private const val SRGB_TO_LINEAR_LOW_COEF = 12.92
    private const val SRGB_TO_LINEAR_HIGH_OFFSET = 0.055
    private const val SRGB_TO_LINEAR_HIGH_DIVISOR = 1.055
    private const val SRGB_TO_LINEAR_GAMMA = 2.4

    // RGB 到 XYZ 转换矩阵系数 (D65 光源)
    private const val RGB_TO_X_R = 0.4124
    private const val RGB_TO_X_G = 0.3576
    private const val RGB_TO_X_B = 0.1805

    private const val RGB_TO_Y_R = 0.2126
    private const val RGB_TO_Y_G = 0.7152
    private const val RGB_TO_Y_B = 0.0722

    private const val RGB_TO_Z_R = 0.0193
    private const val RGB_TO_Z_G = 0.1192
    private const val RGB_TO_Z_B = 0.9505

    private val xyzArrayCache = ThreadLocal<DoubleArray>()

    /**
     * 判断颜色是否为浅色
     * @param color ARGB 格式的颜色值
     * @return true 表示浅色,false 表示深色
     */
    fun isLightColor(@ColorInt color: Int): Boolean {
        return calculateLuminance(color) >= LIGHT_COLOR_THRESHOLD
    }

    /**
     * 获取或创建 ThreadLocal 缓存的数组
     */
    private fun getOrCreateXyzArray(): DoubleArray {
        return xyzArrayCache.get() ?: DoubleArray(3).also { xyzArrayCache.set(it) }
    }

    /**
     * 计算颜色的亮度值
     * @param color ARGB 格式的颜色值
     * @return 亮度值 [0.0, 1.0]
     */
    @FloatRange(from = 0.0, to = 1.0)
    fun calculateLuminance(@ColorInt color: Int): Double {
        val xyzArray = getOrCreateXyzArray()
        convertColorToXyz(color, xyzArray)
        // 亮度对应 XYZ 色彩空间的 Y 分量
        return xyzArray[1] / 100.0
    }

    /**
     * 将 ARGB 颜色转换为 CIE XYZ 色彩空间
     * @param color ARGB 格式的颜色值
     * @param outXyz 输出数组,长度必须为 3
     */
    fun convertColorToXyz(@ColorInt color: Int, outXyz: DoubleArray) {
        val red = android.graphics.Color.red(color)
        val green = android.graphics.Color.green(color)
        val blue = android.graphics.Color.blue(color)
        convertRgbToXyz(red, green, blue, outXyz)
    }

    /**
     * 将 RGB 分量转换为 CIE XYZ 色彩空间
     *
     * 使用 D65 光源和 CIE 2° 标准观察者 (1931)
     *
     * - outXyz[0] 为 X [0, 95.047)
     * - outXyz[1] 为 Y [0, 100)
     * - outXyz[2] 为 Z [0, 108.883)
     *
     * @param red 红色分量 [0, 255]
     * @param green 绿色分量 [0, 255]
     * @param blue 蓝色分量 [0, 255]
     * @param outXyz 输出数组,长度必须为 3
     */
    fun convertRgbToXyz(
        @IntRange(from = 0x0, to = 0xFF) red: Int,
        @IntRange(from = 0x0, to = 0xFF) green: Int,
        @IntRange(from = 0x0, to = 0xFF) blue: Int,
        outXyz: DoubleArray
    ) {
        require(outXyz.size == 3) { "outXyz 数组长度必须为 3" }

        // 将 sRGB 转换为线性 RGB
        val linearR = srgbToLinear(red / 255.0)
        val linearG = srgbToLinear(green / 255.0)
        val linearB = srgbToLinear(blue / 255.0)

        // 应用 RGB 到 XYZ 转换矩阵
        outXyz[0] = 100.0 * (linearR * RGB_TO_X_R + linearG * RGB_TO_X_G + linearB * RGB_TO_X_B)
        outXyz[1] = 100.0 * (linearR * RGB_TO_Y_R + linearG * RGB_TO_Y_G + linearB * RGB_TO_Y_B)
        outXyz[2] = 100.0 * (linearR * RGB_TO_Z_R + linearG * RGB_TO_Z_G + linearB * RGB_TO_Z_B)
    }

    /**
     * sRGB 伽马校正解码 (转换为线性 RGB)
     */
    private fun srgbToLinear(srgbComponent: Double): Double {
        return if (srgbComponent < SRGB_TO_LINEAR_THRESHOLD) {
            srgbComponent / SRGB_TO_LINEAR_LOW_COEF
        } else {
            ((srgbComponent + SRGB_TO_LINEAR_HIGH_OFFSET) / SRGB_TO_LINEAR_HIGH_DIVISOR)
                .pow(SRGB_TO_LINEAR_GAMMA)
        }
    }
}