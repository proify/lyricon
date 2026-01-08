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

package io.github.proify.lyricon.xposed.hook.systemui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.view.doOnAttach
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.proify.android.extensions.dp
import io.github.proify.lyricon.common.util.ResourceMapper
import io.github.proify.lyricon.lyric.style.BasicStyle
import io.github.proify.lyricon.lyric.style.LyricStyle
import io.github.proify.lyricon.xposed.hook.systemui.lyricview.LyricView
import io.github.proify.lyricon.xposed.util.StatusBarColorMonitor
import io.github.proify.lyricon.xposed.util.ViewVisibilityController

class StatusBarViewManager(
    val statusBarView: ViewGroup,
    private var lyricStyle: LyricStyle
) {
    private var lastAnchor = ""
    private var lastInsertionOrder = -1

    private val statusBarViewAttachStateChangeListener = StatusBarViewAttachStateChangeListener()

    private var internalRemoveLyricViewFlag = false
    private val lyricViewAttachStateChangeListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            YLog.info("LyricView attached to window")
        }

        override fun onViewDetachedFromWindow(v: View) {
            if (internalRemoveLyricViewFlag) {
                YLog.info("LyricView has been removed from the internal structure.")
                return
            }
            YLog.info("LyricView detached from window")
            checkLyricViewExists()
        }
    }

    private fun checkLyricViewExists() {
        if (lyricView.isAttachedToWindow) return
        lastAnchor = ""
        lastInsertionOrder = -1
        updateLyricStyle(lyricStyle)
    }

    private val onGlobalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        private var lastPlaying: Boolean = false

        override fun onGlobalLayout() {
            checkUpdateVisibility()
        }

        private fun checkUpdateVisibility() {
            val playing = LyricViewController.isPlaying

            if (!lastPlaying && playing.not()) {
                YLog.info("仅播放时更新歌词可见性")
                return
            }
            visibilityController.applyVisibilityRules(
                rules = lyricStyle.basicStyle.visibilityRules,
                isPlaying = playing
            )
            lastPlaying = playing
        }
    }

    val context: Context = statusBarView.context.applicationContext
    val visibilityController: ViewVisibilityController = ViewVisibilityController(statusBarView)

    val lyricView: LyricView

    init {
        statusBarView.addOnAttachStateChangeListener(statusBarViewAttachStateChangeListener)
        statusBarView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)

        val clockView = getClockView()
        if (clockView != null) {
            StatusBarColorMonitor.hook(clockView.javaClass)
        } else {
            YLog.error("LyricViewManager clock view not found")
        }

        lyricView = createLyricView(lyricStyle)
        lyricView.addOnAttachStateChangeListener(lyricViewAttachStateChangeListener)

        statusBarView.doOnAttach {
            checkLyricViewExists()
        }
    }

    fun getClockView(): View? = statusBarView.findViewById(Constants.clockId)

    fun updateLyricStyle(lyricStyle: LyricStyle) {
        this.lyricStyle = lyricStyle
        val basicStyle = lyricStyle.basicStyle

        val needUpdateLocation =
            lastAnchor != basicStyle.anchor || lastInsertionOrder != basicStyle.insertionOrder

        if (needUpdateLocation) {
            updateLocation(basicStyle)
        } else {
            YLog.info("Lyric location not updated")
        }
        lyricView.updateStyle(lyricStyle)
    }

    private fun updateLocation(baseStyle: BasicStyle) {
        val anchor = baseStyle.anchor
        val anchorId = context.resources.getIdentifier(anchor, "id", context.packageName)

        if (anchorId == 0) {
            YLog.error("Lyric anchor $anchor not found")
            return
        }

        val anchorView = statusBarView.findViewById<View>(anchorId)

        if (anchorView == null) {
            YLog.error("Lyric anchor view $anchor not found")
            return
        }
        val anchorParent = anchorView.parent as? ViewGroup
        if (anchorParent == null) {
            YLog.error("Lyric anchor $anchor parent not found")
            return
        }

        internalRemoveLyricViewFlag = true
        (lyricView.parent as? ViewGroup)?.removeView(lyricView)

        val anchorIndex = anchorParent.indexOfChild(anchorView)

        val lp = lyricView.layoutParams ?: ViewGroup.LayoutParams(
            baseStyle.width.dp,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        if (baseStyle.insertionOrder == BasicStyle.INSERTION_ORDER_AFTER) {
            anchorParent.addView(lyricView, anchorIndex + 1, lp)
        } else {
            anchorParent.addView(lyricView, anchorIndex, lp)
        }

        YLog.info("Lyric added to status bar, anchor $anchor, order ${baseStyle.insertionOrder}")

        lastAnchor = anchor
        lastInsertionOrder = baseStyle.insertionOrder

        internalRemoveLyricViewFlag = false
    }

    private var lastHightlightView: View? = null

    fun hightlightView(idName: String?) {

        fun createHighlightDrawable() = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor("#FF3582FF"))
            cornerRadius = 20.dp.toFloat()
        }

        lastHightlightView?.let {
            lastHightlightView = null
            it.background = null
        }
        if (idName.isNullOrBlank()) return

        val id = ResourceMapper.getIdByName(context, idName)
        val view: View? = statusBarView.findViewById<View>(id)
        if (view == null) {
            YLog.error("Lyric hightlight view $idName not found")
        } else {
            view.background = createHighlightDrawable()
            lastHightlightView = view
        }
    }

    private fun createLyricView(style: LyricStyle) =
        LyricView(context, style, getClockView() as? TextView)

    private class StatusBarViewAttachStateChangeListener : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            // do nothing
        }

        override fun onViewDetachedFromWindow(v: View) {
            // do nothing
        }
    }
}