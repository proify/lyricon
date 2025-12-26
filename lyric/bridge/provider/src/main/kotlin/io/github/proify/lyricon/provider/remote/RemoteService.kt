package io.github.proify.lyricon.provider.remote

interface RemoteService {
    val player: RemotePlayer
    val isActivated: Boolean
    val connectionStatus: ConnectionStatus
    fun addConnectionListener(listener: ConnectionListener)
    fun removeConnectionListener(listener: ConnectionListener)
}