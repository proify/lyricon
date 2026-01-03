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