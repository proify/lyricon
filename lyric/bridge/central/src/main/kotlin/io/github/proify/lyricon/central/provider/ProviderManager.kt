package io.github.proify.lyricon.central.provider

import java.util.concurrent.CopyOnWriteArraySet

object ProviderManager {
    private val providers = CopyOnWriteArraySet<RemoteProvider>()

    fun register(provider: RemoteProvider) {
        if (providers.add(provider)) {
            provider.setDeathRecipient { unregister(provider) }
        }
    }

    fun unregister(provider: RemoteProvider) {
        provider.onDestroy()
        providers -= provider
    }

}