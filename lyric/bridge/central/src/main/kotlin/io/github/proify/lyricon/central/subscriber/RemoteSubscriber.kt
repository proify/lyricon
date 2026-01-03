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

import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import io.github.proify.lyricon.subscriber.IRemoteActivePlayerListener
import io.github.proify.lyricon.subscriber.IRemoteSubscriberBinder
import io.github.proify.lyricon.subscriber.IRemoteSubscriberService
import io.github.proify.lyricon.subscriber.SubscriberInfo

class RemoteSubscriber(private val binder: IRemoteSubscriberBinder) {
    val subscriberInfo: SubscriberInfo = binder.getSubscriberInfo()

    val service: IRemoteSubscriberService = RemoteSubscriberService(this)

    val activePlayerListener: ActivePlayerListenerProxy = ActivePlayerListenerProxy()

    private var deathRecipient: IBinder.DeathRecipient? = null

    fun setRemoteActivePlayerListener(listener: IRemoteActivePlayerListener?) {
        activePlayerListener.setRemoteActivePlayerListener(listener)
    }

    val isAlive: Boolean
        get() = binder.asBinder().isBinderAlive

    @Synchronized
    fun setDeathRecipient(newDeathRecipient: IBinder.DeathRecipient?) {
        if (deathRecipient != null) {
            binder.asBinder().unlinkToDeath(deathRecipient!!, 0)
        }
        if (newDeathRecipient != null) {
            try {
                binder.asBinder().linkToDeath(newDeathRecipient, 0)
            } catch (e: RemoteException) {
                Log.e(TAG, "link to Death failed", e)
            }
        }
        this.deathRecipient = newDeathRecipient
    }

    fun destroy() {
        setDeathRecipient(null)
        setRemoteActivePlayerListener(null)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is RemoteSubscriber) return false
        return this.subscriberInfo == other.subscriberInfo
    }

    override fun hashCode(): Int = subscriberInfo.hashCode()

    companion object {
        private const val TAG = "Subscriber"
    }
}