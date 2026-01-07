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

@file:Suppress("unused")

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
    @ColorInt background: Int,
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