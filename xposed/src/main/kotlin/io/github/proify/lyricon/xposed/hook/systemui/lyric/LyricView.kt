package io.github.proify.lyricon.xposed.hook.systemui.lyric

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import io.github.proify.lyricon.common.extensions.dp
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.lyric.style.LyricStyle
import io.github.proify.lyricon.xposed.util.StatusBarColorMonitor
import io.github.proify.lyricon.xposed.util.StatusColor

class LyricView(
    context: Context,
    initialStyle: LyricStyle,
    linkedTextView: TextView?
) : LinearLayout(context), StatusBarColorMonitor.OnColorChangeListener {

    val logoView = LyricLogo(context)
    val textView = LyricText(context)

    private var currentStyle: LyricStyle = initialStyle
    private var currentStatusColor: StatusColor = StatusColor(Color.BLACK, false)

    init {
        gravity = Gravity.CENTER_VERTICAL
        logoView.linkedTextView = linkedTextView
        textView.linkedTextView = linkedTextView

        addView(logoView)
        addView(textView)

        applyStyle(initialStyle)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        StatusBarColorMonitor.register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        StatusBarColorMonitor.unregister(this)
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
        visibility = if (isPlaying) VISIBLE else GONE
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