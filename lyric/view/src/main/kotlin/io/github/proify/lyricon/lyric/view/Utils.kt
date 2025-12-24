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