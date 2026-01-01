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