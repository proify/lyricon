package io.github.proify.lyricon.lyric.bridge.central.subscriber

import android.util.Log
import io.github.proify.lyricon.lyric.bridge.provider.ProviderInfo
import io.github.proify.lyricon.lyric.bridge.subscriber.IRemoteActivePlayerListener
import io.github.proify.lyricon.lyric.model.Song

class ActivePlayerListenerProxy : OnActivePlayerListener {
    @Volatile
    private var remoteActivePlayerListener: IRemoteActivePlayerListener? = null

    fun setRemoteActivePlayerListener(listener: IRemoteActivePlayerListener?) {
        this.remoteActivePlayerListener = listener
    }

    override fun onActiveProviderChanged(info: ProviderInfo) {
        safeInvoke { it.onActiveProviderChanged(info) }
    }

    override fun onSongChanged(song: Song?) {
        safeInvoke { it.onSongChanged(song) }
    }

    override fun onPlaybackStateChanged(isPlaying: Boolean) {
        safeInvoke { it.onPlaybackStateChanged(isPlaying) }
    }

    override fun onSeekTo(position: Int) {
        safeInvoke { it.onSeekTo(position) }
    }

    override fun onPositionChanged(position: Int) {
        safeInvoke { it.onPositionChanged(position) }
    }

    override fun onPostText(text: String?) {
        safeInvoke { it.onPostText(text) }
    }

    private inline fun safeInvoke(block: (IRemoteActivePlayerListener) -> Unit) {
        val listener = remoteActivePlayerListener ?: return
        try {
            block(listener)
        } catch (e: Exception) {
            Log.w("ActivePlayerListenerProxy", "Exception during callback", e)
        }
    }
}