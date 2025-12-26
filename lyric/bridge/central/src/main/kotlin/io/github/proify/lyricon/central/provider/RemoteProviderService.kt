package io.github.proify.lyricon.central.provider

import io.github.proify.lyricon.central.provider.player.ProviderActivePlayerDispatcher
import io.github.proify.lyricon.central.provider.player.RemotePlayer
import io.github.proify.lyricon.provider.IRemotePlayer
import io.github.proify.lyricon.provider.IRemoteService

class RemoteProviderService(private val provider: RemoteProvider) : IRemoteService.Stub() {

    private val remotePlayer: IRemotePlayer =
        RemotePlayer(provider.providerInfo, ProviderActivePlayerDispatcher)

    override fun getPlayer(): IRemotePlayer = remotePlayer

    fun release() {
        if (remotePlayer is RemotePlayer) {
            remotePlayer.release()
        }
    }

    override fun disconnect() {
        provider.destroy()
    }

}