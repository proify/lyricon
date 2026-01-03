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

import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import io.github.proify.lyricon.provider.IRemoteService
import io.github.proify.lyricon.provider.LyriconProvider
import java.util.concurrent.CopyOnWriteArraySet

internal class RemoteServiceProxy(private val provider: LyriconProvider) : RemoteService,
    RemoteServiceBinder<IRemoteService?> {

    val connectionListeners = CopyOnWriteArraySet<ConnectionListener>()
    private val deathRecipient = DeathRecipient()

    override val player: RemotePlayerProxy = RemotePlayerProxy()
    override var connectionStatus: ConnectionStatus = ConnectionStatus.STATUS_DISCONNECTED
    private var remoteService: IRemoteService? = null

    override fun bindRemoteService(service: IRemoteService?) {
        Log.d(TAG, "Bind service")
        disconnect(false)

        if (service == null) {
            Log.w(TAG, "Service is null")
            return
        }

        val binder = service.asBinder()
        if (!binder.isBinderAlive) {
            Log.w(TAG, "Service is not alive")
            return
        }

        try {
            binder.linkToDeath(deathRecipient, 0)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to link death", e)
            return
        }

        remoteService = service
        player.bindRemoteService(service.player)
        connectionListeners.forEach { it.onConnected(provider) }
    }

    override val isActivated: Boolean
        get() = remoteService?.asBinder()?.isBinderAlive ?: false

    fun disconnect(fromUser: Boolean) {
        Log.d(TAG, "Disconnect")
        connectionStatus =
            if (fromUser) ConnectionStatus.STATUS_DISCONNECTED_BY_USER else ConnectionStatus.STATUS_DISCONNECTED

        player.bindRemoteService(null)

        if (remoteService != null) {
            runCatching {
                remoteService?.asBinder()?.unlinkToDeath(deathRecipient, 0)
            }.onFailure {
                Log.w(TAG, "Failed to unlink death", it)
            }
            runCatching {
                remoteService?.disconnect()
            }.onFailure {
                Log.e(TAG, "Failed to disconnect service", it)
            }
            remoteService = null
            connectionListeners.forEach { it.onDisconnected(provider) }
        }
    }

    override fun addConnectionListener(listener: ConnectionListener) {
        connectionListeners += listener
    }

    override fun removeConnectionListener(listener: ConnectionListener) {
        connectionListeners -= listener
    }

    private inner class DeathRecipient : IBinder.DeathRecipient {
        override fun binderDied() {
            Log.d(TAG, "Service died")
            disconnect(false)
        }
    }

    companion object {
        private const val TAG = "ProviderServiceImpl"
    }
}