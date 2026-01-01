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

package io.github.proify.lyricon.xposed.hook.systemui.lyric

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import io.github.proify.android.extensions.dp
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.lyric.style.LyricStyle
import io.github.proify.lyricon.xposed.util.StatusBarColorMonitor
import io.github.proify.lyricon.xposed.util.StatusColor

class LyricView(
    context: Context,
    initialStyle: LyricStyle,
    linkedTextView: TextView?
) : LinearLayout(context), StatusBarColorMonitor.OnColorChangeListener {

    companion object {
        const val TAG = "LyricView"
        const val VIEW_TAG = "lyricon:lyric_view"
    }

    val logoView = LyricLogoView(context)
    val textView = LyricTextView(context)

    private var currentStyle: LyricStyle = initialStyle
    private var currentStatusColor: StatusColor = StatusColor(Color.BLACK, false)

    private var isPlaying: Boolean = false

    init {
        tag = VIEW_TAG
        gravity = Gravity.CENTER_VERTICAL
        logoView.linkedTextView = linkedTextView
        textView.linkedTextView = linkedTextView

        addView(logoView)
        addView(textView)

        applyStyle(initialStyle)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    fun updateStyle(style: LyricStyle) {
        currentStyle = style
        logoView.applyStyle(style)
        textView.applyStyle(style)
        updateLayoutParams(style)

        invalidate()
        requestLayout()
    }

    private fun applyStyle(style: LyricStyle) {
        currentStyle = style
        logoView.applyStyle(style)
        textView.applyStyle(style)
        updateLayoutParams(style)
    }

    private fun updateLayoutParams(style: LyricStyle) {
        val basicStyle = style.basicStyle
        val margins = basicStyle.margins
        val paddings = basicStyle.paddings

        val params = (layoutParams as? MarginLayoutParams)
            ?: MarginLayoutParams(basicStyle.width.dp, LayoutParams.MATCH_PARENT)

        params.apply {
            width = basicStyle.width.dp
            leftMargin = margins.left.dp
            topMargin = margins.top.dp
            rightMargin = margins.right.dp
            bottomMargin = margins.bottom.dp
        }

        layoutParams = params

        setPadding(
            paddings.left.dp,
            paddings.top.dp,
            paddings.right.dp,
            paddings.bottom.dp
        )
    }

    override fun onColorChange(color: StatusColor) {
        currentStatusColor = color
        children.forEach { child ->
            (child as? StatusBarColorMonitor.OnColorChangeListener)?.onColorChange(color)
        }
    }

    fun setPlaying(isPlaying: Boolean) {
        this.isPlaying = isPlaying
        updateVisibility()
    }

    private fun updateVisibility() {
        if (isPlaying && textView.childCount > 0) {
            visibility = VISIBLE
        } else {
            visibility = GONE
        }
    }

    fun updatePosition(position: Int) {
        textView.setPosition(position)
    }

    fun updateSong(song: Song?) {
        textView.setSong(song)
    }

    fun updateText(text: String?) {
    }
}