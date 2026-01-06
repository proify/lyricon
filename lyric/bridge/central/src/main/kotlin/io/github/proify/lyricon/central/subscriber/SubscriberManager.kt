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

        override fun onPositionChanged(position: Long) =
            broadcast { it.activePlayerListener.onPositionChanged(position) }

        override fun onSeekTo(position: Long) =
            broadcast { it.activePlayerListener.onSeekTo(position) }

        override fun onPostText(text: String?) =
            broadcast { it.activePlayerListener.onPostText(text) }
    }
}