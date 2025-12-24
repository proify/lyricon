package io.github.proify.lyricon.lyric.bridge.central.provider

import java.util.concurrent.CopyOnWriteArraySet

object ProviderManager {
    private val providers = CopyOnWriteArraySet<Provider>()

    fun register(provider: Provider) {
        if (providers.add(provider)) {
            provider.setDeathRecipient { unregister(provider) }
        }
    }

    fun unregister(provider: Provider) {
        provider.destroy()
        providers -= provider
    }

}