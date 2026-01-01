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

import com.highcapable.yukihookapi.hook.log.YLog
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.provider.ProviderInfo
import io.github.proify.lyricon.subscriber.service.OnActivePlayerListener
import io.github.proify.lyricon.xposed.hook.systemui.lyric.LyricView
import io.github.proify.lyricon.xposed.util.LyricPrefs
import io.github.proify.lyricon.xposed.util.StatusBarColorMonitor
import io.github.proify.lyricon.xposed.util.StatusColor

object LyricViewController : OnActivePlayerListener, StatusBarColorMonitor.OnColorChangeListener {

    var activePackage = ""
        private set

    var statusBarViewManager: StatusBarViewManager? = null

    init {
        StatusBarColorMonitor.register(this)
    }

    override fun onActiveProviderChanged(info: ProviderInfo) {
        val packageName = info.playerPackageName

        activePackage = packageName
        LyricPrefs.activePackageName = packageName
        callView {
            it.logoView.providerLogo = info.logo
            YLog.debug("LyricViewController.onActiveProviderChanged: $info")
        }
    }

    override fun onSongChanged(song: Song?) {
        YLog.debug("LyricViewController.onSongChanged: $song")
        callView {
            it.updateSong(song)
            it.logoView.refreshLogoDisplay()
        }
    }

    override fun onPlaybackStateChanged(isPlaying: Boolean) {
        YLog.debug("LyricViewController.onPlaybackStateChanged: $isPlaying")
        callView {
            it.setPlaying(isPlaying)
        }
    }

    override fun onPositionChanged(position: Int) {
        callView {
            it.updatePosition(position)
        }
    }

    override fun onSeekTo(position: Int) {
        callView {
            it.updatePosition(position)
        }
    }

    override fun onPostText(text: String?) {
        callView {
            it.updateText(text)
        }
    }

    private inline fun callView(crossinline action: (LyricView) -> Unit) {
        statusBarViewManager?.getLyricView()?.let { action(it) }
    }

    override fun onColorChange(color: StatusColor) {
        callView { it.onColorChange(color) }
    }
}