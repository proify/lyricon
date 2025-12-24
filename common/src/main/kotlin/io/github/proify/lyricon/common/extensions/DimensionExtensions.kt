@file:Suppress("unused")

package io.github.proify.lyricon.common.extensions

import android.content.res.Resources
import android.util.TypedValue
import kotlin.math.roundToInt

private val displayMetrics = Resources.getSystem().displayMetrics

/**
 * 将当前的浮动数值转换为 dp 单位的整数。
 * 四舍五入后转换为整型，适用于 UI 尺寸的设置。
 */
val Float.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        displayMetrics
    ).roundToInt()

/**
 * 将当前的浮动数值转换为 dp 单位的整数。
 * 四舍五入后转换为整型，适用于 UI 尺寸的设置。
 */
val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        displayMetrics
    ).roundToInt()

/**
 * 将当前的浮动数值转换为 sp 单位的浮动数值。
 * 适用于字体大小等文本相关尺寸的转换。
 */
val Float.sp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        displayMetrics
    )

/**
 * 将当前的浮动数值转换为 sp 单位的浮动数值。
 * 适用于字体大小等文本相关尺寸的转换。
 */
val Int.sp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        displayMetrics
    )