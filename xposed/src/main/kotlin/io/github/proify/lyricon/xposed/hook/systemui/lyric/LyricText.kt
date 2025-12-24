package io.github.proify.lyricon.xposed.hook.systemui.lyric

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.fonts.FontStyle
import android.widget.TextView
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.proify.lyricon.common.extensions.dp
import io.github.proify.lyricon.common.extensions.sp
import io.github.proify.lyricon.common.util.CommonUtils
import io.github.proify.lyricon.lyric.style.LyricStyle
import io.github.proify.lyricon.lyric.style.TextStyle
import io.github.proify.lyricon.lyric.view.MainMarqueeConfig
import io.github.proify.lyricon.lyric.view.MainSyllableConfig
import io.github.proify.lyricon.lyric.view.MiniLyricsView
import io.github.proify.lyricon.xposed.util.StatusBarColorMonitor
import io.github.proify.lyricon.xposed.util.StatusColor
import java.io.File
import kotlin.math.min

class LyricText(context: Context) : MiniLyricsView(context),
    StatusBarColorMonitor.OnColorChangeListener {

    private var statusColor: StatusColor = StatusColor(Color.BLACK, false)
    var linkedTextView: TextView? = null

    private var style: LyricStyle? = null

    fun applyStyle(style: LyricStyle) {
        this.style = style

        val textStyle = style.packageStyle.text

        val margins = textStyle.margins
        val paddings = textStyle.paddings

        var lp = layoutParams as? LayoutParams
        if (lp == null) {
            lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        lp.leftMargin = margins.left.dp
        lp.topMargin = margins.top.dp
        lp.rightMargin = margins.right.dp
        lp.bottomMargin = margins.bottom.dp

        layoutParams = lp

        setPadding(
            paddings.left.dp,
            paddings.top.dp,
            paddings.right.dp,
            paddings.bottom.dp
        )

        val customTextColor = textStyle.color(statusColor.isLight)
        val primaryTextColor =
            if (textStyle.enableCustomTextColor && customTextColor != null) customTextColor.normal else statusColor.color

        val marquee = MainMarqueeConfig().apply {
            scrollSpeed = textStyle.marqueeSpeed
            ghostSpacing = textStyle.marqueeGhostSpacing

            if (textStyle.marqueeDelayEnable) {
                initialDelay = textStyle.marqueeInitialDelay
                loopDelay = textStyle.marqueeLoopDelay
            } else {
                initialDelay = 0
                loopDelay = 0
            }
            repeatCount = if (textStyle.marqueeRepeatUnlimited) -1 else textStyle.marqueeRepeatCount
            stopAtEnd = textStyle.marqueeStopAtEnd
        }

        val typeface = createTypeface(textStyle)

        val defaultTextSize = linkedTextView?.textSize ?: 14f.sp
        val fontSize = if (textStyle.textSize > 0) textStyle.textSize.sp else defaultTextSize
        setStyle(
            getStyle().apply {
                primary.apply {
                    textColor = primaryTextColor
                    textSize = fontSize
                    this.typeface = typeface
                }

                secondary.apply {
                    textColor = primaryTextColor
                    textSize = fontSize * .8f
                    this.typeface = typeface
                }
                this.marquee = marquee
                this.syllable.apply {
                    if (textStyle.enableCustomTextColor && customTextColor != null) {
                        backgroundColor = customTextColor.background
                        highlightColor = customTextColor.highlight
                    } else {
                        backgroundColor = CommonUtils.setAlphaComponent(statusColor.color, 0.5f)
                        highlightColor = statusColor.color
                    }
                }
            }
        )
    }

    private fun createTypeface(textStyle: TextStyle): Typeface {
        val typeFacePath = textStyle.typeFace
        var baseTypeface: Typeface? = null

        if (typeFacePath != null && typeFacePath.isNotBlank()) {
            val file = File(typeFacePath)
            if (file.exists()) {
                try {
                    baseTypeface = Typeface.createFromFile(file)
                } catch (e: Exception) {
                    YLog.error("Failed to load typeface from $file", e)
                }
            }
        }

        if (baseTypeface == null && linkedTextView != null) {
            baseTypeface = linkedTextView?.typeface
        }
        if (baseTypeface == null) {
            baseTypeface = Typeface.DEFAULT
        }

        val weight = min(FontStyle.FONT_WEIGHT_MAX, textStyle.fontWeight)
        if (weight > 0) {
            return Typeface.create(baseTypeface, weight, textStyle.typeFaceItalic)
        }
        val style = when {
            textStyle.typeFaceBold && textStyle.typeFaceItalic -> Typeface.BOLD_ITALIC
            textStyle.typeFaceBold -> Typeface.BOLD
            textStyle.typeFaceItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
        return Typeface.create(baseTypeface, style)
    }

    override fun onColorChange(color: StatusColor) {
        this.statusColor = color
        updateColor()
    }

    private fun updateColor() {
        val style = this.style ?: return
        val textStyle = style.packageStyle.text

        val customTextColor = textStyle.color(statusColor.isLight)
        val primaryTextColor =
            if (textStyle.enableCustomTextColor && customTextColor != null) customTextColor.normal else statusColor.color

        val syllable = MainSyllableConfig().apply {
            if (textStyle.enableCustomTextColor && customTextColor != null) {
                backgroundColor = customTextColor.background
                highlightColor = customTextColor.highlight
            } else {
                backgroundColor = CommonUtils.setAlphaComponent(statusColor.color, 0.5f)
                highlightColor = statusColor.color
            }
        }

        setStyle(
            getStyle().apply {
                primary.apply {
                    textColor = primaryTextColor
                }
                secondary.apply {
                    textColor = primaryTextColor
                }
                this.syllable = syllable
            }
        )
        invalidate()
    }

}