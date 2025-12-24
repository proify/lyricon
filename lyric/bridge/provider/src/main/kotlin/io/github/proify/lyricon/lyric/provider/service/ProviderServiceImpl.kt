package io.github.proify.lyricon.lyric.provider.service

import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import io.github.proify.lyricon.lyric.bridge.provider.IRemoteProviderService
import io.github.proify.lyricon.lyric.provider.LyriconProvider
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicReference

internal class ProviderServiceImpl(private val provider: LyriconProvider) : ProviderServiceProxy {

    val connectionListeners = CopyOnWriteArraySet<ConnectionListener>()
    private val deathRecipient = DeathRecipient()
    private val remoteServiceRef = AtomicReference<IRemoteProviderService?>(null)

    override val player: PlayerProxy = PlayerImpl()

    override var connectionStatus: ConnectionStatus = ConnectionStatus.STATUS_DISCONNECTED

    private val remoteService: IRemoteProviderService?
        get() = remoteServiceRef.get()

    override fun bindService(service: IRemoteProviderService?) {
        disconnect(false)

        if (service == null) return

        val binder = service.asBinder()
        if (!binder.isBinderAlive) return

        try {
            binder.linkToDeath(deathRecipient, 0)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to link death", e)
            return
        }

        remoteServiceRef.set(service)
        player.bindPlayer(service.player)

        connectionListeners.forEach { it.onConnected(provider) }
    }

    override val isActivated: Boolean
        get() = remoteService?.asBinder()?.isBinderAlive ?: false

    /**
     * 断开与中心服务的连接
     *
     * 主动断开提供者与中心服务之间的连接。
     * 断开后将无法继续控制播放器，直到重新注册。
     *
     * 注意事项：
     * - 断开后 [isActivated] 将变为 false
     * - 会触发所有监听器的 [ConnectionListener.onDisconnected] 回调
     * - 如果 fromUser 为 true，状态码会变为 [ConnectionStatus.STATUS_DISCONNECTED_BY_USER]
     *
     * @param fromUser true 表示用户主动断开（如调用 [io.github.proify.lyricon.lyric.provider.LyriconProvider.unregister]）；
     *                 false 表示系统自动断开（如服务崩溃、超时等）
     */
    fun disconnect(fromUser: Boolean) {
        connectionStatus = if (fromUser) {
            ConnectionStatus.STATUS_DISCONNECTED_BY_USER
        } else {
            ConnectionStatus.STATUS_DISCONNECTED
        }

        player.bindPlayer(null)

        val service = remoteServiceRef.getAndSet(null)
        if (service != null) {
            runCatching {
                service.asBinder().unlinkToDeath(deathRecipient, 0)
            }.onFailure {
                Log.w(TAG, "Failed to unlink death", it)
            }

            runCatching {
                service.disconnect()
            }.onFailure {
                Log.e(TAG, "Failed to disconnect service", it)
            }

            connectionListeners.forEach {
                it.onDisconnected(provider)
            }
        }
    }

    override fun addConnectionListener(listener: ConnectionListener) {
        connectionListeners += listener
    }

    override fun addConnectionListener(builder: ConnectionListenerBuilder.() -> Unit): ConnectionListener {
        val listener = ConnectionListenerBuilder().apply(builder).build()
        connectionListeners += listener
        return listener
    }

    override fun removeConnectionListener(listener: ConnectionListener) {
        connectionListeners -= listener
    }

    private inner class DeathRecipient : IBinder.DeathRecipient {
        override fun binderDied() {
            Log.w(TAG, "Service died")
            disconnect(false)
        }
    }

    companion object {
        private const val TAG = "ProviderServiceImpl"
    }
}