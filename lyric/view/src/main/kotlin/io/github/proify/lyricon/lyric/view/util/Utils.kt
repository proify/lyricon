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

package io.github.proify.lyricon.lyric.view.util

import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import androidx.core.view.isVisible
import kotlin.math.roundToInt

internal fun View.hide() {
    if (visibility != View.GONE) visibility = View.GONE
}

internal fun View.show() {
    if (visibility != View.VISIBLE) visibility = View.VISIBLE
}

internal inline var View.visible: Boolean
    get() = isVisible
    set(value) {
        val newVisibility = if (value) View.VISIBLE else View.GONE
        if (visibility != newVisibility) visibility = newVisibility
    }

internal inline val Int.dp: Int
    get() = toFloat().dp.roundToInt()

internal inline val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

internal inline val Float.sp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    )