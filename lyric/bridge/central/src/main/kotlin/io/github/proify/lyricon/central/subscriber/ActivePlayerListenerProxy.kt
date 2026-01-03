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