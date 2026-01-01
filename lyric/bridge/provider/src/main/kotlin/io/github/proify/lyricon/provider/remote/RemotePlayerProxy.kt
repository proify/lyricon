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

package io.github.proify.lyricon.provider.remote

import android.os.SharedMemory
import android.util.Log
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.provider.IRemotePlayer
import java.nio.ByteBuffer
import kotlin.math.max

internal class RemotePlayerProxy : RemotePlayer, RemoteServiceBinder<IRemotePlayer?> {

    @Volatile
    private var remoteService: IRemotePlayer? = null

    private var positionSharedMemory: SharedMemory? = null
    private var positionByteBuffer: ByteBuffer? = null

    @Synchronized
    private fun clearBinding() {
        try {
            positionByteBuffer?.let { SharedMemory.unmap(it) }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to unmap position buffer", e)
        } finally {
            positionByteBuffer = null
        }

        try {
            positionSharedMemory?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to close shared memory", e)
        } finally {
            positionSharedMemory = null
        }

        remoteService = null
    }

    @Synchronized
    override fun bindRemoteService(service: IRemotePlayer?) {
        Log.d(TAG, "bindRemoteService")
        clearBinding()
        remoteService = service

        try {
            val sharedMemory = remoteService?.positionUpdateSharedMemory
            positionSharedMemory = sharedMemory
            positionByteBuffer = positionSharedMemory?.mapReadWrite()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to obtain position shared memory", e)
        }
    }

    override fun setSong(song: Song?): Boolean =
        executeRemoteCall { it.setSong(song) }

    override fun setPlaybackState(isPlaying: Boolean): Boolean =
        executeRemoteCall { it.setPlaybackState(isPlaying) }

    override fun seekTo(position: Int): Boolean =
        executeRemoteCall { it.seekTo(max(0, position)) }

    override fun setPosition(position: Int): Boolean {
        return try {
            positionByteBuffer?.putInt(0, position)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write position to shared buffer", e)
            false
        }
    }

    override fun setPositionUpdateInterval(interval: Int): Boolean =
        executeRemoteCall { it.setPositionUpdateInterval(interval) }

    override fun sendText(text: String?): Boolean =
        executeRemoteCall { it.sendText(text) }

    override val isActivated: Boolean
        get() = remoteService?.asBinder()?.isBinderAlive == true

    private inline fun executeRemoteCall(block: (IRemotePlayer) -> Any?): Boolean {
        val service = remoteService ?: return false
        return try {
            val result = block(service)
            when (result) {
                is Boolean -> result
                else -> true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Remote call failed", e)
            false
        }
    }

    companion object {
        private const val TAG = "RemotePlayerProxy"
    }
}