package io.github.proify.lyricon.subscriber.service

import android.os.Handler
import android.os.Looper
import android.util.Log
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.provider.ProviderInfo
import io.github.proify.lyricon.subscriber.IRemoteActivePlayerListener
import java.util.concurrent.CopyOnWriteArraySet

internal class ActivePlayerListenerDispatcher : IRemoteActivePlayerListener.Stub() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val listenerSet = CopyOnWriteArraySet<OnActivePlayerListener>()

    override fun onActiveProviderChanged(info: ProviderInfo) =
        dispatchEvent { it.onActiveProviderChanged(info) }

    override fun onSongChanged(song: Song?) =
        dispatchEvent { it.onSongChanged(song) }

    override fun onPlaybackStateChanged(isPlaying: Boolean) =
        dispatchEvent { it.onPlaybackStateChanged(isPlaying) }

    override fun onSeekTo(position: Int) =
        dispatchEvent { it.onSeekTo(position) }

    override fun onPositionChanged(position: Int) =
        dispatchEvent { it.onPositionChanged(position) }

    override fun onPostText(text: String?) =
        dispatchEvent { it.onPostText(text) }

    fun addListener(listener: OnActivePlayerListener) = listenerSet.add(listener)

    fun removeListener(listener: OnActivePlayerListener) = listenerSet.remove(listener)

    private inline fun dispatchEvent(crossinline action: (OnActivePlayerListener) -> Unit) {
        val listenerAction = ListenerAction { listener -> action(listener) }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            notifyAllListeners(listenerAction)
        } else {
            mainHandler.post { notifyAllListeners(listenerAction) }
        }
    }

    private fun notifyAllListeners(action: ListenerAction) {
        for (listener in listenerSet) {
            try {
                action.invoke(listener)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to invoke listener", t)
            }
        }
    }

    fun interface ListenerAction {
        fun invoke(listener: OnActivePlayerListener)
    }

    companion object {
        private const val TAG = "ActivePlayerListenerDispatcher"
    }
}