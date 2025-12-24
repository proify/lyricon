package io.github.proify.lyricon.lyric.bridge.central

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.github.proify.lyricon.lyric.bridge.central.provider.Provider
import io.github.proify.lyricon.lyric.bridge.central.provider.ProviderManager
import io.github.proify.lyricon.lyric.bridge.central.subscriber.Subscriber
import io.github.proify.lyricon.lyric.bridge.central.subscriber.SubscriberManager
import io.github.proify.lyricon.lyric.bridge.core.Constants
import io.github.proify.lyricon.lyric.bridge.provider.IRemoteProviderBinder
import io.github.proify.lyricon.lyric.bridge.subscriber.IRemoteSubscriberBinder

internal object CentralReceiver : BroadcastReceiver() {
    private const val TAG = "CentralReceiver"

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
            IRemoteProviderBinder::class ->
                IRemoteProviderBinder.Stub.asInterface(binder) as? T

            IRemoteSubscriberBinder::class ->
                IRemoteSubscriberBinder.Stub.asInterface(binder) as? T

            else -> null.also {
                Log.e(TAG, "Unknown binder type")
            }
        }
    }

    private fun registerProvider(intent: Intent) {
        Log.d(TAG, "registerProvider")

        val providerBinder = getBinder<IRemoteProviderBinder>(intent) ?: return
        var provider: Provider? = null

        try {
            provider = Provider(providerBinder).also {
                val info = it.providerInfo
                if (info.modulePackageName.isNullOrBlank() ||
                    info.musicAppPackageName.isNullOrBlank()
                ) {
                    Log.e(TAG, "Provider module package name is null or blank: ${info}")
                    return
                }
            }

            ProviderManager.register(provider)
            providerBinder.onRegistrationCallback(provider.service)
            Log.d(
                TAG,
                "Provider registered: ${provider.providerInfo.modulePackageName}/  ${provider.providerInfo.musicAppPackageName}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Provider registration failed", e)
            provider?.let { ProviderManager.unregister(it) }
        }
    }

    private fun registerSubscriber(intent: Intent) {
        Log.d(TAG, "registerSubscriber")

        val subscriberBinder = getBinder<IRemoteSubscriberBinder>(intent) ?: return
        var subscriber: Subscriber? = null

        try {
            subscriber = Subscriber(subscriberBinder).also {
                val info = it.subscriberInfo
                if (info.packageName.isNullOrBlank()) {
                    Log.e(TAG, "Subscriber package name is null or blank: ${info}")
                    return
                }
            }

            SubscriberManager.register(subscriber)
            subscriberBinder.onRegistrationCallback(subscriber.service)
            Log.d(TAG, "Subscriber registered: ${subscriber.subscriberInfo.packageName}")
        } catch (e: Exception) {
            Log.e(TAG, "Subscriber registration failed", e)
            subscriber?.let { SubscriberManager.unregister(it) }
        }
    }
}