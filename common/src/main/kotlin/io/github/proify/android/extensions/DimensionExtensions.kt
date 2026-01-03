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