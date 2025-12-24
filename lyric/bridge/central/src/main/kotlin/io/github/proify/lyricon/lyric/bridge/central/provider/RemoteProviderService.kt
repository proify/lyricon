package io.github.proify.lyricon.lyric.bridge.central.provider

import io.github.proify.lyricon.lyric.bridge.central.provider.player.ProviderActivePlayerDispatcher
import io.github.proify.lyricon.lyric.bridge.central.provider.player.RemotePlayer
import io.github.proify.lyricon.lyric.bridge.provider.IRemotePlayer
import io.github.proify.lyricon.lyric.bridge.provider.IRemoteProviderService

class RemoteProviderService(private val provider: Provider) : IRemoteProviderService.Stub() {
    private val remotePlayer: IRemotePlayer =
        RemotePlayer(provider.providerInfo, ProviderActivePlayerDispatcher)

    override fun getPlayer(): IRemotePlayer = remotePlayer

    override fun disconnect() =
        ProviderManager.unregister(provider)
}