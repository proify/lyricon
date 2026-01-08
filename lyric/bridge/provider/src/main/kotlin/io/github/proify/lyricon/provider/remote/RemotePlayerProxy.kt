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

package io.github.proify.lyricon.provider.remote

import android.os.SharedMemory
import android.util.Log
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.provider.IRemotePlayer
import io.github.proify.lyricon.provider.extensions.deflate
import kotlinx.serialization.json.Json
import java.nio.ByteBuffer
import kotlin.math.max

internal class RemotePlayerProxy : RemotePlayer, RemoteServiceBinder<IRemotePlayer?> {
    companion object {
        private const val TAG = "RemotePlayerProxy"
        private val json = Json { ignoreUnknownKeys = true }
    }

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

    override fun setSong(song: Song?) =
        executeRemoteCall {
            val bytes = if (song != null) {
                json.encodeToString(song)
                    .toByteArray()
                    .deflate()
            } else null

            it.setSong(bytes)
        }

    override fun setPlaybackState(isPlaying: Boolean) =
        executeRemoteCall { it.setPlaybackState(isPlaying) }

    override fun seekTo(position: Long): Boolean =
        executeRemoteCall { it.seekTo(max(0, position)) }

    override fun setPosition(position: Long) = try {
        positionByteBuffer?.putLong(0, position)
        true
    } catch (e: Exception) {
        Log.e(TAG, "Failed to write position to shared buffer", e)
        false
    }

    override fun setPositionUpdateInterval(interval: Int) =
        executeRemoteCall { it.setPositionUpdateInterval(interval) }

    override fun sendText(text: String?) = executeRemoteCall { it.sendText(text) }

    override val isActivated get() = remoteService?.asBinder()?.isBinderAlive == true

    private inline fun executeRemoteCall(block: (IRemotePlayer) -> Any?): Boolean {
        val service = remoteService ?: return false
        return try {
            when (val result = block(service)) {
                is Boolean -> result
                else -> true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Remote call failed", e)
            false
        }
    }
}