package io.github.proify.android.extensions

import android.content.res.Resources
import android.util.TypedValue
import kotlin.math.roundToInt

private val displayMetrics = Resources.getSystem().displayMetrics

/**
 *  将 DP 转换为像素 (PX) 整数。适用于布局尺寸。
 */
val Float.dp: Int
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, displayMetrics)
        .roundToInt()

/**
 *  @see [Float.dp]
 * */
val Int.dp: Int get() = this.toFloat().dp

/**
 * 将 SP 转换为像素 (PX) 浮点数。
 */
val Float.sp: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, displayMetrics)

/** @see [Float.sp] */
val Int.sp: Float get() = this.toFloat().sp