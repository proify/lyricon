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

package io.github.proify.lyricon.lyric.view

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.core.view.size
import io.github.proify.lyricon.lyric.model.DoubleLyricLine
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.lyric.model.filterByPositionOrPrevious

open class MiniLyricsView(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    companion object {
        const val TAG = "MiniLyricsView"
    }

    private val activeLines = mutableListOf<DoubleLyricLine>()

    private var song: Song? = null

    private var config: DoubleLyricConfig = DoubleLyricConfig()

    private val myLayoutTransition =
        LayoutTransition().apply { enableTransitionType(LayoutTransition.CHANGING) }

    init {
        orientation = VERTICAL
        layoutTransition = myLayoutTransition
    }

    fun setSong(song: Song?) {
        reset()
        if (song != null) {
            val newSong = song.copy()
            fillGapAtStart(newSong)
            this.song = newSong
        }
    }

    private fun createDoubleLineView(line: DoubleLyricLine): DoubleLineView {
        val view = DoubleLineView(context)
        view.line = line
        view.setStyle(config)
        view.setMainLyricPlayListener(object : LyricPlayListener {

            override fun onPlayStarted(view: LyricLineView) {
                updateViewsVisibility()
            }

            override fun onPlayEnded(view: LyricLineView) {
                updateViewsVisibility()
            }

            override fun onPlayProgress(view: LyricLineView, total: Float, progress: Float) {}
        })
        view.setSecondaryLyricPlayListener(object : LyricPlayListener {

            override fun onPlayStarted(view: LyricLineView) {
                view.visible = true
                updateViewsVisibility()
            }

            override fun onPlayEnded(view: LyricLineView) {
                updateViewsVisibility()
            }

            override fun onPlayProgress(view: LyricLineView, total: Float, progress: Float) {}
        })
        return view
    }

    private fun updateViewsVisibility() {

        for (i in 0..<childCount) {
            val view = getChildAt(i) as DoubleLineView
            val first = getChildAt(0) as DoubleLineView

            view.visibility = VISIBLE
            view.main.setTextSize(config.primary.textSize)
            view.secondary.setTextSize(config.secondary.textSize)

            if (i == 0) {
                if (view.secondary.isVisible
                    && view.main.syllable.isPlayFinished()
                    && childCount > 1
                ) {
                    view.main.visibility = GONE
                }
            } else if (i == 1) {
                if (first.main.isVisible && first.secondary.isVisible) {
                    view.visibility = GONE
                } else {
                    if (first.isVisible && first.main.isVisible) {
                        view.main.setTextSize(config.secondary.textSize)
                        view.secondary.setTextSize(config.primary.textSize)
                    }
                }
            } else {
                view.visibility = GONE
            }
        }
    }


    fun reset() {
        removeAllViews()
    }

    override fun removeAllViews() {
        setLayoutTransition(null)
        super.removeAllViews()
        updateViewsVisibility()
    }

    override fun removeView(view: View?) {
        super.removeView(view)
    }

    fun setPosition(position: Int) {
        //val start = SystemClock.elapsedRealtime()
        val matches = findActiveLines(position)
        updateActiveViews(matches)
        updateViewsPosition(position)
        //Log.d(TAG, "updateLines: ${SystemClock.elapsedRealtime() - start}ms")
    }

    fun updateActiveViews(matches: List<DoubleLyricLine>) {
        val addToList: MutableList<DoubleLineView> = mutableListOf()
        val removeToList: MutableList<DoubleLineView> = mutableListOf()

        for (i in 0..<size) {
            val view: DoubleLineView = getChildAt(i) as DoubleLineView
            if (view.line != null && matches.contains(view.line).not()) {
                removeToList.add(view)
            }
        }

        matches.forEach { line ->
            if (!activeLines.contains(line)) {
                val view: DoubleLineView = createDoubleLineView(line)
                addToList.add(view)
            }
        }

        if (addToList.isEmpty() && removeToList.isEmpty()) return

        val single = activeLines.size == 1
                && removeToList.size == 1
                && addToList.size == 1

        var updateViewsVisibility = false
        if (single) {
            val recycleView: DoubleLineView = getChildAt(0) as DoubleLineView

            val newLine = addToList[0].line

            activeLines[0] = newLine!!

            recycleView.line = newLine
            updateViewsVisibility = true
        } else {
            removeToList.forEach { view ->
                removeView(view)
                activeLines.remove(view.line)
                updateViewsVisibility = true
            }

            addToList.forEach { view ->
                val line = view.line!!
                activeLines.add(line)
                autoAddView(view)
                updateViewsVisibility = true
            }
        }

        if (updateViewsVisibility) updateViewsVisibility()
    }

    fun autoAddView(view: DoubleLineView) {
        if (layoutTransition == null && isNotEmpty()) {
            setLayoutTransition(myLayoutTransition)
        }
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        addView(view, lp)
    }

    private fun findActiveLines(progress: Int): List<DoubleLyricLine> {
        return song?.lyrics?.filterByPositionOrPrevious(progress) ?: emptyList()
    }

    private fun updateViewsPosition(progress: Int) {
        for (i in 0..<childCount) {
            val child = getChildAt(i)
            if (child is DoubleLineView) child.setPosition(progress)
        }
    }

    fun setStyle(config: DoubleLyricConfig) {
        this.config = config
        for (i in 0..<childCount) {
            val child = getChildAt(i)
            if (child is DoubleLineView) child.setStyle(config)
        }
    }

    fun getStyle() = config

    fun fillGapAtStart(song: Song) {
        val songTitle = getSongTitle(song) ?: return

        val lyrics = song.lyrics?.toMutableList() ?: mutableListOf()

        if (lyrics.isEmpty()) {
            val duration = if (song.duration > 0) song.duration else 1145141919
            lyrics.add(SongTitleLine(songTitle).apply {
                begin = duration
                end = duration
                this.duration = duration
            })
        } else {
            val firstLine = lyrics.first()
            if (firstLine.begin > 0) {
                lyrics.add(0, SongTitleLine(songTitle).apply {
                    begin = 0
                    end = firstLine.begin
                    duration = firstLine.begin
                })
            }
        }

        song.lyrics = lyrics
    }

    private fun SongTitleLine(songTitle: String): DoubleLyricLine {
        return DoubleLyricLine(text = songTitle)
    }

    private fun getSongTitle(song: Song): String? {
        val hasName = song.name?.isNotBlank() ?: false
        val hasArtist = song.artist?.isNotBlank() ?: false

        return when {
            hasName && hasArtist -> "${song.name} - ${song.artist}"
            hasName -> song.name
            else -> null
        }
    }

}