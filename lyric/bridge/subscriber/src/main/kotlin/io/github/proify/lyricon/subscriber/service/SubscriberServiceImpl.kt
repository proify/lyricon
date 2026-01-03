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

import android.os.IBinder
import android.util.Log
import io.github.proify.lyricon.subscriber.IRemoteSubscriberService
import io.github.proify.lyricon.subscriber.LyricSubscriber

internal class SubscriberServiceImpl(private val subscriber: LyricSubscriber?) :
    SubscriberServiceProxy {
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