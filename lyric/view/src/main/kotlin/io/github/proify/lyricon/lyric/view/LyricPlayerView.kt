package io.github.proify.lyricon.lyric.view

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.forEach
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.core.view.size
import io.github.proify.lyricon.lyric.model.DoubleLyricLine
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.lyric.model.extensions.filterByPositionOrPrevious
import io.github.proify.lyricon.lyric.view.line.LyricLineView
import io.github.proify.lyricon.lyric.view.util.visible

open class LyricPlayerView(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    private val activeLines = mutableListOf<DoubleLyricLine>()
    private var song: Song? = null
    private var config: DoubleLyricConfig = DoubleLyricConfig()

    private val myLayoutTransition =
        LayoutTransition().apply { enableTransitionType(LayoutTransition.CHANGING) }

    private val tempViewsToRemove = mutableListOf<DoubleLineView>()
    private val tempViewsToAdd = mutableListOf<DoubleLineView>()

    private val reusableLayoutParams =
        LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

    private val mainLyricPlayListener = object : LyricPlayListener {
        override fun onPlayStarted(view: LyricLineView) {
            updateViewsVisibility()
        }

        override fun onPlayEnded(view: LyricLineView) {
            updateViewsVisibility()
        }
    }

    private val secondaryLyricPlayListener = object : LyricPlayListener {
        override fun onPlayStarted(view: LyricLineView) {
            view.visible = true
            updateViewsVisibility()
        }

        override fun onPlayEnded(view: LyricLineView) {
            updateViewsVisibility()
        }
    }

    init {
        orientation = VERTICAL
        layoutTransition = myLayoutTransition
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        reset()
    }

    fun setSong(song: Song?) {
        reset()
        if (song != null) {
            val newSong = song.copy()
            fillGapAtStart(newSong)
            this.song = newSong
        }
    }

    fun reset() {
        removeAllViews()
        activeLines.clear() // 清空活动列表
    }

    override fun removeAllViews() {
        setLayoutTransition(null)
        super.removeAllViews()
    }

    private fun createDoubleLineView(line: DoubleLyricLine): DoubleLineView {
        val view = DoubleLineView(context)
        view.line = line
        view.setStyle(config)
        view.setMainLyricPlayListener(mainLyricPlayListener)
        view.setSecondaryLyricPlayListener(secondaryLyricPlayListener)
        return view
    }

    private fun updateViewsVisibility() {
        val childCount = childCount
        if (childCount == 0) return

        val first = getChildAt(0) as DoubleLineView

        for (i in 0 until childCount) {
            val view = getChildAt(i) as DoubleLineView

            view.visibility = VISIBLE
            view.main.setTextSize(config.primary.textSize)
            view.secondary.setTextSize(config.secondary.textSize)

            when (i) {
                0 -> {
                    if (view.secondary.isVisible
                        && view.main.syllable.isPlayFinished()
                        && childCount > 1
                    ) {
                        view.main.visibility = GONE
                    }
                }

                1 -> {
                    if (first.main.isVisible && first.secondary.isVisible) {
                        view.visibility = GONE
                    } else {
                        if (first.isVisible && first.main.isVisible) {
                            view.main.setTextSize(config.secondary.textSize)
                            view.secondary.setTextSize(config.primary.textSize)
                        }
                    }
                }

                else -> {
                    view.visibility = GONE
                }
            }
        }
    }

    override fun removeView(view: View?) {
        super.removeView(view)
    }

    fun setPosition(position: Long) {
        val matches = findActiveLines(position)
        updateActiveViews(matches)

        forEach { if (it is DoubleLineView) it.setPosition(position) }
    }

    private fun updateActiveViews(matches: List<DoubleLyricLine>) {
        tempViewsToRemove.clear()
        tempViewsToAdd.clear()

        // 找出需要移除的视图
        val currentSize = size
        for (i in 0 until currentSize) {
            val view = getChildAt(i) as DoubleLineView
            val line = view.line
            if (line != null && line !in matches) {
                tempViewsToRemove.add(view)
            }
        }

        // 找出需要添加的视图
        val matchesSize = matches.size
        for (i in 0 until matchesSize) {
            val line = matches[i]
            if (line !in activeLines) {
                tempViewsToAdd.add(createDoubleLineView(line))
            }
        }

        // 如果没有变化,直接返回
        if (tempViewsToRemove.isEmpty() && tempViewsToAdd.isEmpty()) return

        // 优化:单个视图替换的情况
        val isSingleViewSwap = activeLines.size == 1
                && tempViewsToRemove.size == 1
                && tempViewsToAdd.size == 1

        if (isSingleViewSwap) {
            val recycleView = getChildAt(0) as DoubleLineView
            val newLine = tempViewsToAdd[0].line

            newLine?.let { activeLines[0] = it }
            recycleView.line = newLine
        } else {
            // 批量处理移除
            val removeSize = tempViewsToRemove.size
            for (i in 0 until removeSize) {
                val view = tempViewsToRemove[i]
                removeView(view)
                activeLines.remove(view.line)
            }

            // 批量处理添加
            val addSize = tempViewsToAdd.size
            for (i in 0 until addSize) {
                val view = tempViewsToAdd[i]
                view.line?.let { activeLines.add(it) }
                autoAddView(view)
            }
        }

        updateViewsVisibility()
    }

    fun autoAddView(view: DoubleLineView) {
        if (layoutTransition == null && isNotEmpty()) {
            setLayoutTransition(myLayoutTransition)
        }
        // 复用LayoutParams对象
        addView(view, reusableLayoutParams)
    }

    private fun findActiveLines(progress: Long) =
        song?.lyrics?.filterByPositionOrPrevious(progress) ?: emptyList()

    fun setStyle(config: DoubleLyricConfig): LyricPlayerView = apply {
        this.config = config
        forEach { if (it is DoubleLineView) it.setStyle(config) }
    }

    fun getStyle(): DoubleLyricConfig = config

    fun fillGapAtStart(song: Song) {
        val songTitle = getSongTitle(song) ?: return

        val lyrics = song.lyrics?.toMutableList() ?: mutableListOf()

        if (lyrics.isEmpty()) {
            val duration = if (song.duration > 0) song.duration else 1145141919L
            lyrics.add(songTitleLine(songTitle).apply {
                begin = duration
                end = duration
                this.duration = duration
            })
        } else {
            val firstLine = lyrics.first()
            if (firstLine.begin > 0) {
                lyrics.add(0, songTitleLine(songTitle).apply {
                    begin = 0
                    end = firstLine.begin
                    duration = firstLine.begin
                })
            }
        }

        song.lyrics = lyrics
    }

    private fun songTitleLine(songTitle: String) = DoubleLyricLine(text = songTitle)

    private fun getSongTitle(song: Song): String? {
        val hasName = song.name?.isNotBlank() ?: false
        val hasArtist = song.artist?.isNotBlank() ?: false

        return when {
            hasName && hasArtist -> "${song.name} - ${song.artist}"
            hasName -> song.name
            else -> null
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }
}