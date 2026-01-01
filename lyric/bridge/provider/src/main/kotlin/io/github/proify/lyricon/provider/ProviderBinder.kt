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

package io.github.proify.lyricon.provider

import android.util.Log
import io.github.proify.lyricon.provider.remote.RemoteServiceBinder
import java.util.concurrent.CopyOnWriteArraySet

internal class ProviderBinder(
    private val provider: LyriconProvider,
    private val providerService: ProviderService,
    private val remoteServiceBinder: RemoteServiceBinder<IRemoteService?>
) : IProviderBinder.Stub() {

    private val registrationCallbacks = CopyOnWriteArraySet<RegistrationCallback>()

    fun addRegistrationCallback(callback: RegistrationCallback) {
        registrationCallbacks.add(callback)
    }

    fun removeRegistrationCallback(callback: RegistrationCallback) {
        registrationCallbacks.remove(callback)
    }

    fun interface RegistrationCallback {
        fun onRegistered()
    }

    override fun onRegistrationCallback(remoteProviderService: IRemoteService?) {
        remoteServiceBinder.bindRemoteService(remoteProviderService)
        notifyRegistrationCallbacks()
    }

    override fun getProviderService(): IProviderService = providerService

    override fun getProviderInfo(): ProviderInfo = provider.providerInfo

    private fun notifyRegistrationCallbacks() {
        registrationCallbacks.forEach {
            try {
                it.onRegistered()
            } catch (e: Exception) {
                Log.e(TAG, "Error in callback", e)
            }
        }
    }

    companion object {
        private const val TAG = "RemoteProviderBinder"
    }
}