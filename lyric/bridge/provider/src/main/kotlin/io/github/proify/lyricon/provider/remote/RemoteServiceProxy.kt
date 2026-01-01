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