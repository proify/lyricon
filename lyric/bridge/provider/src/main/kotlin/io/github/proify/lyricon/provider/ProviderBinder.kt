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

package io.github.proify.lyricon.provider

import io.github.proify.lyricon.provider.remote.RemoteServiceBinder
import kotlinx.serialization.json.Json
import java.util.concurrent.CopyOnWriteArraySet

internal class ProviderBinder(
    private val provider: LyriconProvider,
    private val providerService: ProviderService,
    private val remoteServiceBinder: RemoteServiceBinder<IRemoteService?>
) : IProviderBinder.Stub() {

    private val providerInfoByteArray by lazy {
        json.encodeToString(ProviderInfo.serializer(), provider.providerInfo).toByteArray()
    }

    companion object {
        val json = Json { ignoreUnknownKeys = true }
    }

    private val registrationCallbacks = CopyOnWriteArraySet<OnRegistrationCallback>()

    fun addRegistrationCallback(callback: OnRegistrationCallback) =
        registrationCallbacks.add(callback)

    fun removeRegistrationCallback(callback: OnRegistrationCallback) =
        registrationCallbacks.remove(callback)

    override fun onRegistrationCallback(remoteProviderService: IRemoteService?) {
        remoteServiceBinder.bindRemoteService(remoteProviderService)
        registrationCallbacks.forEach { it.onRegistered() }
    }

    override fun getProviderService(): IProviderService = providerService
    override fun getProviderInfo(): ByteArray = providerInfoByteArray

    interface OnRegistrationCallback {
        fun onRegistered()
    }
}