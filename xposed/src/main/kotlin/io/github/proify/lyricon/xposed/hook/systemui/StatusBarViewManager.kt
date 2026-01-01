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

package io.github.proify.lyricon.xposed.hook.systemui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.proify.android.extensions.dp
import io.github.proify.lyricon.lyric.style.BasicStyle
import io.github.proify.lyricon.xposed.hook.systemui.lyric.LyricView
import io.github.proify.lyricon.xposed.util.LyricPrefs
import io.github.proify.lyricon.xposed.util.StatusBarColorMonitor

class StatusBarViewManager(
    val statusBarView: ViewGroup
) {

    val context: Context = statusBarView.context.applicationContext
    private var lyricView: LyricView

    private val viewAttachStateChangeListener: ViewAttachStateChangeListener =
        ViewAttachStateChangeListener()

    init {
        lyricView = createLyricView()

        val clockView = getClockView()
        if (clockView != null) {
            StatusBarColorMonitor.hook(clockView.javaClass)
        } else {
            YLog.error("LyricViewManager clock view not found")
        }
    }

    fun initialize() {
        runCatching {
            updateLyricView()
        }.getOrElse {
            YLog.error("LyricViewManager initialize failed", it)
        }
        statusBarView.addOnAttachStateChangeListener(viewAttachStateChangeListener)
    }

    fun getClockView(): View? = statusBarView.findViewById(Constants.clockId)

    fun getLyricView(): LyricView {
        return lyricView
    }

    private var lastAnchor = ""
    private var lastInsertionOrder = -1

    fun updateLyricView() {
        val lyricStyle = LyricPrefs.getLyricStyle()
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

        (lyricView.parent as? ViewGroup)?.removeView(lyricView)

        val anchorIndex = anchorParent.indexOfChild(anchorView)

        var lp = lyricView.layoutParams
        if (lp == null) {
            lp = ViewGroup.LayoutParams(
                baseStyle.width.dp,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        if (baseStyle.insertionOrder == BasicStyle.INSERTION_ORDER_AFTER) {
            anchorParent.addView(lyricView, anchorIndex + 1, lp)
        } else {
            anchorParent.addView(lyricView, anchorIndex, lp)
        }

        YLog.info("Lyric added to status bar, anchor $anchor, order ${baseStyle.insertionOrder}")
        lastAnchor = anchor
        lastInsertionOrder = baseStyle.insertionOrder
    }

    private fun createLyricView(): LyricView {
        val style = LyricPrefs.getLyricStyle()
        val view = LyricView(context, style, getClockView() as? TextView)
        return view
    }

    private class ViewAttachStateChangeListener : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {

        }

        override fun onViewDetachedFromWindow(v: View) {

        }
    }

}