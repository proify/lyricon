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

package io.github.proify.lyricon.subscriber.service

import android.os.IBinder
import android.util.Log
import io.github.proify.lyricon.subscriber.IRemoteSubscriberService
import io.github.proify.lyricon.subscriber.LyricSubscriber

internal class SubscriberServiceImpl(private val subscriber: LyricSubscriber?) : SubscriberServiceProxy {
    private val dispatcher = ActivePlayerListenerDispatcher()
    private val deathRecipient: DeathRecipient = DeathRecipient()

    private var remoteService: IRemoteSubscriberService? = null
    override var connectionListener: ConnectionListener? = null

    override fun bindService(service: IRemoteSubscriberService?) {
        if (service == null) {
            disconnect()
            return
        }
        disconnect()
        this.remoteService = service
        try {
            remoteService?.bindActivePlayerListener(dispatcher)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind focus player listener", e)
        }
        remoteService?.asBinder()?.linkToDeath(deathRecipient, 0)
        connectionListener?.onConnected(subscriber)
    }

    override fun registerActivePlayerListener(listener: OnActivePlayerListener) {
        dispatcher.addListener(listener)
    }

    override fun unregisterActivePlayerListener(listener: OnActivePlayerListener) {
        dispatcher.removeListener(listener)
    }

    override val isActivate: Boolean
        get() = remoteService?.asBinder()?.isBinderAlive ?: false


    override fun disconnect() {
        remoteService ?: return
        try {
            remoteService?.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Disconnect remote service failed", e)
        }
        remoteService?.asBinder()?.unlinkToDeath(deathRecipient, 0)
        remoteService = null
        connectionListener?.onDisconnected(subscriber)
    }

    private inner class DeathRecipient : IBinder.DeathRecipient {
        override fun binderDied() = disconnect()
    }

    companion object {
        private const val TAG = "SubscriberServiceImpl"
    }
}