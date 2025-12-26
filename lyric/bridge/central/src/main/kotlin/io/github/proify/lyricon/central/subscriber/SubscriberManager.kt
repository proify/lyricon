package io.github.proify.lyricon.central.subscriber

import android.util.Log
import io.github.proify.lyricon.central.provider.player.ProviderActivePlayerDispatcher
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.provider.ProviderInfo
import java.util.concurrent.CopyOnWriteArraySet

internal object SubscriberManager {
    const val TAG = "SubscriberManager"
    const val DEBUG = false
    private val subscribers = CopyOnWriteArraySet<RemoteSubscriber>()

    init {
        ProviderActivePlayerDispatcher.addOnActivePlayerListener(PlayerEventBroadcaster)
    }

    fun register(subscriber: RemoteSubscriber) {
        if (subscribers.add(subscriber)) {
            subscriber.setDeathRecipient { unregister(subscriber) }
        }
    }

    fun unregister(subscriber: RemoteSubscriber) {
        subscriber.destroy()
        subscribers -= subscriber
    }

    object PlayerEventBroadcaster : OnActivePlayerListener {

        private inline fun broadcast(
            block: (RemoteSubscriber) -> Unit
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