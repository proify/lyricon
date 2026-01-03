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

package io.github.proify.lyricon.lyric.view

import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import androidx.core.view.isVisible

internal fun View.hide() {
    visibility = View.GONE
}

internal fun View.show() {
    visibility = View.VISIBLE
}

internal inline var View.visible: Boolean
    get() = isVisible
    set(value) {
        val nowVisibility = visibility
        val newVisibility = if (value) View.VISIBLE else View.GONE
        if (nowVisibility == newVisibility) return
        visibility = if (value) View.VISIBLE else View.GONE
    }

internal inline val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

internal inline val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

internal inline val Float.sp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    )