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

package io.github.proify.lyricon.central

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.github.proify.lyricon.central.provider.ProviderManager
import io.github.proify.lyricon.central.provider.RemoteProvider
import io.github.proify.lyricon.central.subscriber.RemoteSubscriber
import io.github.proify.lyricon.central.subscriber.SubscriberManager
import io.github.proify.lyricon.provider.IProviderBinder
import io.github.proify.lyricon.provider.ProviderInfo
import io.github.proify.lyricon.subscriber.IRemoteSubscriberBinder
import kotlinx.serialization.json.Json

internal object CentralReceiver : BroadcastReceiver() {
    private const val TAG = "CentralReceiver"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Constants.ACTION_REGISTER_PROVIDER -> registerProvider(intent)
            Constants.ACTION_REGISTER_SUBSCRIBER -> registerSubscriber(intent)
        }
    }

    private inline fun <reified T> getBinder(intent: Intent): T? {
        val bundle = intent.getBundleExtra(Constants.EXTRA_BUNDLE) ?: return null
        val binder = bundle.getBinder(Constants.EXTRA_BINDER) ?: return null
        return when (T::class) {
            IProviderBinder::class ->
                IProviderBinder.Stub.asInterface(binder) as? T

            IRemoteSubscriberBinder::class ->
                IRemoteSubscriberBinder.Stub.asInterface(binder) as? T

            else -> null.also {
                Log.e(TAG, "Unknown binder type")
            }
        }
    }

    private fun registerProvider(intent: Intent) {
        Log.d(TAG, "registerProvider")

        val binder = getBinder<IProviderBinder>(intent) ?: return
        var provider: RemoteProvider? = null

        try {
            val providerInfoByteArray: ByteArray? = binder.providerInfo
            val providerInfo = providerInfoByteArray?.let {
                json.decodeFromString(
                    ProviderInfo.serializer(),
                    it.toString(Charsets.UTF_8)
                )
            }
            if (providerInfo == null
                || providerInfo.providerPackageName.isNullOrBlank()
                || providerInfo.playerPackageName.isNullOrBlank()
            ) {
                Log.e(TAG, "Provider info is invalid")
                return
            }
            provider = RemoteProvider(binder, providerInfo)
            ProviderManager.register(provider)

            binder.onRegistrationCallback(provider.service)
            Log.d(
                TAG,
                "Provider registered: ${provider.providerInfo.providerPackageName}/  ${provider.providerInfo.playerPackageName}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Provider registration failed", e)
            provider?.let { ProviderManager.unregister(it) }
        }
    }

    private fun registerSubscriber(intent: Intent) {
        Log.d(TAG, "registerSubscriber")

        val binder = getBinder<IRemoteSubscriberBinder>(intent) ?: return
        var subscriber: RemoteSubscriber? = null

        try {
            val subscriberInfo = binder.subscriberInfo
            if (subscriberInfo.packageName.isNullOrBlank()) {
                Log.e(TAG, "Subscriber info is invalid")
                return
            }

            subscriber = RemoteSubscriber(binder)
            SubscriberManager.register(subscriber)

            binder.onRegistrationCallback(subscriber.service)
            Log.d(TAG, "Subscriber registered: ${subscriber.subscriberInfo.packageName}")
        } catch (e: Exception) {
            Log.e(TAG, "Subscriber registration failed", e)
            subscriber?.let { SubscriberManager.unregister(it) }
        }
    }
}