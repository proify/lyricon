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