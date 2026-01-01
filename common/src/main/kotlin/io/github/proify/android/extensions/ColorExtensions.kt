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

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.graphics.ColorUtils

/**
 * 检查该颜色是否属于深色调
 *
 * ```kotlin
 * val textColor = if (bgColor.isDarkColor) Color.WHITE else Color.BLACK
 * ```
 */
val Int.isDarkColor: Boolean
    get() = isDarkAgainst(Color.WHITE)

/**
 * 判断当前颜色叠加在 [background] 上后，在视觉上是否呈现为深色。
 *
 * 基于 WCAG 感知亮度标准。
 *
 * @param background 背景色，用于合成半透明色的最终效果。
 * @param threshold 亮度阈值 (0.0-1.0)，低于此值视为深色。
 */
fun Int.isDarkAgainst(
    @ColorInt background: Int = Color.WHITE,
    @FloatRange(from = 0.0, to = 1.0) threshold: Double = 0.5
): Boolean {
    val alpha = Color.alpha(this)
    if (alpha == 0) return ColorUtils.calculateLuminance(background) < threshold

    val finalColor = if (alpha == 255) this else ColorUtils.compositeColors(this, background)
    return ColorUtils.calculateLuminance(finalColor) < threshold
}

/**
 * 设置颜色的透明度分量。
 *
 * ```kotlin
 * val halfTranslucentBlue = Color.BLUE.setColorAlpha(0.5f)
 * ```
 * @param alpha 透明度比例 (0.0 - 1.0)。
 */
@ColorInt
fun Int.setColorAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): Int {
    val alphaInt = (alpha.coerceIn(0f, 1f) * 255f + 0.5f).toInt()
    return ColorUtils.setAlphaComponent(this, alphaInt)
}

/**
 * 根据当前颜色的亮度，自动获取一个对比色
 *
 * ```kotlin
 * textView.setTextColor(bgColor.getContrastingColor())
 * ```
 */
@ColorInt
fun Int.getContrastingColor(
    @ColorInt darkColor: Int = Color.BLACK,
    @ColorInt lightColor: Int = Color.WHITE
): Int = if (this.isDarkColor) lightColor else darkColor