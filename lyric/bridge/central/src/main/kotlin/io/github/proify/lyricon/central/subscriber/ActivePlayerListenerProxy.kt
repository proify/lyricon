package io.github.proify.lyricon.central.subscriber

import android.util.Log
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.provider.ProviderInfo
import io.github.proify.lyricon.subscriber.IRemoteActivePlayerListener

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