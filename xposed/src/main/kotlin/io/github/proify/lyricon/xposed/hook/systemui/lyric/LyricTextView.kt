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

package io.github.proify.lyricon.xposed.hook.systemui.lyric

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.fonts.FontStyle
import android.view.ViewGroup
import android.widget.TextView
import io.github.proify.android.extensions.dp
import io.github.proify.android.extensions.setColorAlpha
import io.github.proify.android.extensions.sp
import io.github.proify.lyricon.lyric.style.LyricStyle
import io.github.proify.lyricon.lyric.style.TextStyle
import io.github.proify.lyricon.lyric.view.MainMarqueeConfig
import io.github.proify.lyricon.lyric.view.MainSyllableConfig
import io.github.proify.lyricon.lyric.view.MiniLyricsView
import io.github.proify.lyricon.xposed.util.StatusBarColorMonitor
import io.github.proify.lyricon.xposed.util.StatusColor
import java.io.File
import kotlin.math.min

class LyricTextView(context: Context) : MiniLyricsView(context),
    StatusBarColorMonitor.OnColorChangeListener {

    private var currentStatusColor = StatusColor(Color.BLACK, false)
    private var currentLyricStyle: LyricStyle? = null

    companion object {
        const val VIEW_TAG = "lyricon:text_view"
    }

    var linkedTextView: TextView? = null

    init {
        tag = VIEW_TAG
    }

    fun applyStyle(style: LyricStyle) {
        this.currentLyricStyle = style
        val textStyle = style.packageStyle.text

        updateViewLayout(textStyle)

        val config = getStyle().apply {
            val typeface = resolveTypeface(textStyle)
            val fontSize =
                if (textStyle.textSize > 0) textStyle.textSize.sp else (linkedTextView?.textSize
                    ?: 14f.sp)

            primary.apply {
                this.textColor = resolvePrimaryColor(textStyle)
                this.textSize = fontSize
                this.typeface = typeface
            }

            secondary.apply {
                this.textColor = primary.textColor
                this.textSize = fontSize * 0.8f
                this.typeface = typeface
            }

            this.marquee = buildMarqueeConfig(textStyle)
            this.syllable = buildSyllableConfig(textStyle)
        }

        setStyle(config)
    }

    override fun onColorChange(color: StatusColor) {
        this.currentStatusColor = color
        refreshColors()
    }

    private fun refreshColors() {
        val textStyle = currentLyricStyle?.packageStyle?.text ?: return

        val primaryColor = resolvePrimaryColor(textStyle)
        val syllableConfig = buildSyllableConfig(textStyle)

        setStyle(getStyle().apply {
            primary.textColor = primaryColor
            secondary.textColor = primaryColor
            this.syllable = syllableConfig
        })
        invalidate()
    }

    private fun updateViewLayout(textStyle: TextStyle) {
        val margins = textStyle.margins
        val paddings = textStyle.paddings

        val params = (layoutParams as? ViewGroup.MarginLayoutParams)
            ?: ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

        params.setMargins(margins.left.dp, margins.top.dp, margins.right.dp, margins.bottom.dp)
        layoutParams = params

        setPadding(paddings.left.dp, paddings.top.dp, paddings.right.dp, paddings.bottom.dp)
    }

    private fun buildMarqueeConfig(textStyle: TextStyle) = MainMarqueeConfig().apply {
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

    private fun buildSyllableConfig(textStyle: TextStyle) = MainSyllableConfig().apply {
        val customColor = textStyle.color(currentStatusColor.lightMode)
        val isCustomEnabled = textStyle.enableCustomTextColor && customColor != null

        backgroundColor = when {
            isCustomEnabled && customColor.background != 0 -> customColor.background
            else -> currentStatusColor.color.setColorAlpha(0.5f)
        }

        highlightColor = when {
            isCustomEnabled && customColor.highlight != 0 -> customColor.highlight
            else -> currentStatusColor.color
        }
    }

    private fun resolvePrimaryColor(textStyle: TextStyle): Int {
        val customColor = textStyle.color(currentStatusColor.lightMode)
        return if (textStyle.enableCustomTextColor && customColor != null && customColor.normal != 0) {
            customColor.normal
        } else {
            currentStatusColor.color
        }
    }

    private fun resolveTypeface(textStyle: TextStyle): Typeface {
        val baseTypeface = textStyle.typeFace?.takeIf { it.isNotBlank() }?.let { path ->
            val file = File(path)
            if (file.exists()) {
                runCatching { Typeface.createFromFile(file) }.getOrNull()
            } else null
        } ?: linkedTextView?.typeface ?: Typeface.DEFAULT

        return if (textStyle.fontWeight > 0) {
            Typeface.create(
                baseTypeface,
                min(FontStyle.FONT_WEIGHT_MAX, textStyle.fontWeight),
                textStyle.typeFaceItalic
            )
        } else {
            val style = when {
                textStyle.typeFaceBold && textStyle.typeFaceItalic -> Typeface.BOLD_ITALIC
                textStyle.typeFaceBold -> Typeface.BOLD
                textStyle.typeFaceItalic -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }
            Typeface.create(baseTypeface, style)
        }
    }


}