package io.github.proify.lyricon.lyric.bridge.central.subscriber

import android.util.Log
import io.github.proify.lyricon.lyric.bridge.central.provider.player.ProviderActivePlayerDispatcher
import io.github.proify.lyricon.lyric.bridge.provider.ProviderInfo
import io.github.proify.lyricon.lyric.model.Song
import java.util.concurrent.CopyOnWriteArraySet

internal object SubscriberManager {
    const val TAG = "SubscriberManager"
    const val DEBUG = false
    private val subscribers = CopyOnWriteArraySet<Subscriber>()

    init {
        ProviderActivePlayerDispatcher.addOnActivePlayerListener(PlayerEventBroadcaster)
    }

    fun register(subscriber: Subscriber) {
        if (subscribers.add(subscriber)) {
            subscriber.setDeathRecipient { unregister(subscriber) }
        }
    }

    fun unregister(subscriber: Subscriber) {
        subscriber.destroy()
        subscribers -= subscriber
    }

    object PlayerEventBroadcaster : OnActivePlayerListener {

        private inline fun broadcast(
            block: (Subscriber) -> Unit
        ) {
            if (DEBUG) Log.d(TAG, "Broadcasting event to ${subscribers.size} subscribers")
            subscribers.forEach(block)
        }

        override fun onActiveProviderChanged(info: ProviderInfo) =
            broadcast { it.activePlayerListener.onActiveProviderChanged(info) }

        override fun onSongChanged(song: Song?) =
            broadcast { it.activePlayerListener.onSongChanged(song) }

        override fun onPlaybackStateChanged(isPlaying: Boolean) =
            broadcast { it.activePlayerListener.onPlaybackStateChanged(isPlaying) }

        override fun onPositionChanged(position: Int) =
            broadcast { it.activePlayerListener.onPositionChanged(position) }

        override fun onSeekTo(position: Int) =
            broadcast { it.activePlayerListener.onSeekTo(position) }

        override fun onPostText(text: String?) =
            broadcast { it.activePlayerListener.onPostText(text) }
    }
}