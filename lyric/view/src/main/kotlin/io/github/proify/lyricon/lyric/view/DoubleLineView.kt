package io.github.proify.lyricon.lyric.view

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import io.github.proify.lyricon.lyric.model.DoubleLyricLine
import io.github.proify.lyricon.lyric.model.LyricLine

class DoubleLineView(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    val customLayoutTransition = LayoutTransition().apply {
        enableTransitionType(LayoutTransition.CHANGING)
    }

    init {
        layoutTransition = customLayoutTransition
    }

    var line: DoubleLyricLine? = null
        set(value) {
            field = value
            setMainLine(value)
            setSecondaryLine(value)
        }

    val main: LyricLineView =
        LyricLineView(context)

    val secondary: LyricLineView =
        LyricLineView(context)

    init {
        orientation = VERTICAL
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        addView(main, lp)
        addView(secondary, lp)
    }

    private fun setMainLine(lyricLine: DoubleLyricLine?) {
        val line = if (lyricLine == null) {
            LyricLine()
        } else {
            LyricLine(
                begin = lyricLine.begin,
                end = lyricLine.end,
                duration = lyricLine.duration,
                isAlignedRight = lyricLine.isAlignedRight,
                extraMetadata = lyricLine.extraMetadata,
                text = lyricLine.text,
                words = lyricLine.words,
            )
        }

        main.setLyric(line)
        //main.visible = line.words.isNotEmpty()
    }

    fun setMainLyricPlayListener(listener: LyricPlayListener?) {
        main.syllable.lyricPlayListener = listener
    }

    fun setSecondaryLyricPlayListener(listener: LyricPlayListener?) {
        secondary.syllable.lyricPlayListener = listener
    }

    private fun setSecondaryLine(source: DoubleLyricLine?) {
        val line = LyricLine().apply {
            if (source == null) return@apply
            begin = source.begin
            end = source.end
            isAlignedRight = source.isAlignedRight
            text = source.secondaryText
            words = source.secondaryWords
        }
        secondary.setLyric(line)
        secondary.visible = false
    }

    fun setPosition(position: Int) {
        main.setPosition(position)
        secondary.setPosition(position)
    }

    fun setStyle(config: DoubleLyricConfig) {
        setStyle(main, config.primary, config.marquee, config.syllable)
        setStyle(secondary, config.secondary, config.marquee, config.syllable)
    }

    fun setStyle(
        view: LyricLineView,
        textConfig: TextConfig,
        marqueeConfig: MarqueeConfig,
        syllableConfig: SyllableConfig
    ) {
        val config = LyricLineConfig(
            textConfig,
            marqueeConfig,
            syllableConfig
        )
        view.setStyle(config)
        invalidate()

    }

}