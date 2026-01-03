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

package io.github.proify.lyricon.central.provider

import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import io.github.proify.lyricon.provider.IProviderBinder
import io.github.proify.lyricon.provider.ProviderInfo

class RemoteProvider(private val binder: IProviderBinder) {
    val providerInfo: ProviderInfo = binder.getProviderInfo()
    val service: RemoteProviderService = RemoteProviderService(this)

    private var deathRecipient: IBinder.DeathRecipient? = null

    fun setDeathRecipient(newDeathRecipient: IBinder.DeathRecipient?) {
        deathRecipient?.let { binder.asBinder().unlinkToDeath(it, 0) }

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
        ProviderManager.unregister(this)
    }

    fun onDestroy() {
        service.release()
        setDeathRecipient(null)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is RemoteProvider) return false
        return this.providerInfo == other.providerInfo
    }

    override fun hashCode(): Int {
        return providerInfo.hashCode()
    }

    override fun toString(): String {
        return "RemoteProvider{" + this.providerInfo + "}"
    }

    companion object {
        private const val TAG = "Provider"
    }
}