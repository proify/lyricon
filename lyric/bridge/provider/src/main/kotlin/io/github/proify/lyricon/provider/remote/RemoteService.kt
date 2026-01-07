@file:Suppress("unused", "UnusedReceiverParameter")

package io.github.proify.lyricon.provider.remote

import io.github.proify.lyricon.provider.LyriconProvider

/**
 * 中央服务控制接口
 */
interface RemoteService {

    /**
     * 播放器服务
     */
    val player: RemotePlayer

    /**
     * 是否处于激活状态
     */
    val isActivated: Boolean

    /**
     * 当前连接状态
     */
    val connectionStatus: ConnectionStatus

    /**
     * 注册连接状态监听器
     *
     * @param listener 监听器实例
     * @see ConnectionListener
     */
    fun addConnectionListener(listener: ConnectionListener): Boolean

    /**
     * 移除已注册的连接状态监听器
     *
     * @param listener 之前注册的监听器实例
     */
    fun removeConnectionListener(listener: ConnectionListener): Boolean
}

/**
 * 根据构建器构建连接状态监听器
 */
fun RemoteService.buildConnectionListener(block: ConnectionListenerBuilder.() -> Unit): ConnectionListener {
    val builder = ConnectionListenerBuilder().apply(block)
    return object : ConnectionListener {
        override fun onConnected(provider: LyriconProvider) {
            builder.onConnected?.invoke(provider)
        }

        override fun onReconnected(provider: LyriconProvider) {
            builder.onReconnected?.invoke(provider)
        }

        override fun onDisconnected(provider: LyriconProvider) {
            builder.onDisconnected?.invoke(provider)
        }

        override fun onConnectTimeout(provider: LyriconProvider) {
            builder.onConnectTimeout?.invoke(provider)
        }
    }
}

/**
 * 直接注册连接状态监听器并返回
 */
fun RemoteService.addOnConnectionListener(block: ConnectionListenerBuilder.() -> Unit): ConnectionListener {
    val listener = buildConnectionListener(block)
    addConnectionListener(listener)
    return listener
}

/**
 * 连接状态监听器构建器
 */
class ConnectionListenerBuilder(
    var onConnected: ((LyriconProvider) -> Unit)? = null,
    var onReconnected: ((LyriconProvider) -> Unit)? = null,
    var onDisconnected: ((LyriconProvider) -> Unit)? = null,
    var onConnectTimeout: ((LyriconProvider) -> Unit)? = null
) {
    fun onConnected(block: (LyriconProvider) -> Unit): ConnectionListenerBuilder =
        apply { onConnected = block }

    fun onReconnected(block: (LyriconProvider) -> Unit): ConnectionListenerBuilder =
        apply { onReconnected = block }

    fun onDisconnected(block: (LyriconProvider) -> Unit): ConnectionListenerBuilder =
        apply { onDisconnected = block }

    fun onConnectTimeout(block: (LyriconProvider) -> Unit): ConnectionListenerBuilder =
        apply { onConnectTimeout = block }
}