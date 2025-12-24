package io.github.proify.lyricon.xposed.hook.systemui

import com.highcapable.yukihookapi.hook.log.YLog
import io.github.proify.lyricon.lyric.bridge.provider.ProviderInfo
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.lyric.subscriber.service.OnActivePlayerListener
import io.github.proify.lyricon.xposed.hook.systemui.lyric.LyricView
import io.github.proify.lyricon.xposed.util.LyricPrefs

object LyricViewController : OnActivePlayerListener {

    var activePackage = ""
        private set

    var statusBarViewManager: StatusBarViewManager? = null

    override fun onActiveProviderChanged(info: ProviderInfo) {
        val packageName = info.musicAppPackageName

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
}