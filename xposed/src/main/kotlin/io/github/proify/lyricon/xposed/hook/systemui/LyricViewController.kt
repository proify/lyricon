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