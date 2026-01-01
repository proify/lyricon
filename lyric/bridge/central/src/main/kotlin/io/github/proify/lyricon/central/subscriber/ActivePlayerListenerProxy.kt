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