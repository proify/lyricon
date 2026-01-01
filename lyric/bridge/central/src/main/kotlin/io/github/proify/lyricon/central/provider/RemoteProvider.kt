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