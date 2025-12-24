package io.github.proify.lyricon.lyric.provider.service

import io.github.proify.lyricon.lyric.provider.LyriconProvider

/**
 * Provider 连接状态监听器
 *
 * 用于监听 Provider 与中心服务之间的连接状态变化。
 * 所有回调方法都有默认空实现，可以选择性地重写需要的方法。
 *
 * 使用示例：
 * ```kotlin
 * provider.service.addConnectionListener(object : ConnectionListener {
 *     override fun onConnected(provider: Provider) {
 *         // 处理连接成功
 *     }
 *
 *     override fun onDisconnected(provider: Provider) {
 *         // 处理断开连接
 *     }
 * })
 * ```
 */
interface ConnectionListener {

    /**
     * 当首次成功连接到中心服务时调用
     *
     * 此方法在 Provider 初次建立连接时触发，适合执行初始化操作。
     * 如果是断线重连，则会触发 [onReconnected] 而不是此方法。
     *
     * @param provider 已连接的 Provider 实例
     */
    fun onConnected(provider: LyriconProvider) {}

    /**
     * 当断开连接后重新建立连接时调用
     *
     * 此方法用于区分首次连接和重连场景，允许应用执行不同的恢复逻辑。
     * 例如：重新同步数据、重新订阅事件等。
     *
     * @param provider 重新连接的 Provider 实例
     */
    fun onReconnected(provider: LyriconProvider) {}

    /**
     * 当与中心服务断开连接时调用
     *
     * 可能的触发原因：
     * - 中心服务崩溃或被杀死
     * - 用户主动调用 [LyriconProvider.unregister]
     * - 网络或 IPC 通信异常
     * - 系统资源不足导致服务终止
     *
     * @param provider 已断开连接的 Provider 实例
     */
    fun onDisconnected(provider: LyriconProvider) {}

    /**
     * 当连接尝试超时时调用
     *
     * 在调用 [LyriconProvider.register] 后，如果在规定时间内未收到中心服务的响应，
     * 则会触发此回调。通常表示中心服务未运行或无法访问。
     *
     * 建议操作：
     * - 提示用户检查中心服务状态
     * - 稍后自动重试连接
     * - 记录日志用于问题诊断
     *
     * @param provider 连接超时的 Provider 实例
     */
    fun onConnectTimeout(provider: LyriconProvider) {}
}

/**
 * 连接监听器适配器
 *
 * 提供更简洁的方式来创建只关心特定事件的监听器。
 *
 * 使用示例：
 * ```kotlin
 * addConnectionListener(
 *     ConnectionListenerAdapter(
 *         onConnected = { provider ->
 *             // 只处理连接成功事件
 *         }
 *     )
 * )
 * ```
 */
class ConnectionListenerAdapter(
    private val onConnected: ((LyriconProvider) -> Unit)? = null,
    private val onReconnected: ((LyriconProvider) -> Unit)? = null,
    private val onDisconnected: ((LyriconProvider) -> Unit)? = null,
    private val onConnectTimeout: ((LyriconProvider) -> Unit)? = null
) : ConnectionListener {

    override fun onConnected(provider: LyriconProvider) {
        onConnected?.invoke(provider)
    }

    override fun onReconnected(provider: LyriconProvider) {
        onReconnected?.invoke(provider)
    }

    override fun onDisconnected(provider: LyriconProvider) {
        onDisconnected?.invoke(provider)
    }

    override fun onConnectTimeout(provider: LyriconProvider) {
        onConnectTimeout?.invoke(provider)
    }
}

/**
 * DSL 方式创建连接监听器
 *
 * 使用示例：
 * ```kotlin
 * provider.service.addConnectionListener(connectionListener {
 *     onConnected { provider ->
 *         // 处理连接
 *     }
 *     onDisconnected { provider ->
 *         // 处理断开
 *     }
 * })
 * ```
 */
inline fun connectionListener(builder: ConnectionListenerBuilder.() -> Unit): ConnectionListener {
    return ConnectionListenerBuilder().apply(builder).build()
}

/**
 * 连接监听器构建器
 */
class ConnectionListenerBuilder {
    private var onConnectedCallback: ((LyriconProvider) -> Unit)? = null
    private var onReconnectedCallback: ((LyriconProvider) -> Unit)? = null
    private var onDisconnectedCallback: ((LyriconProvider) -> Unit)? = null
    private var onConnectTimeoutCallback: ((LyriconProvider) -> Unit)? = null

    fun onConnected(callback: (LyriconProvider) -> Unit) {
        onConnectedCallback = callback
    }

    fun onReconnected(callback: (LyriconProvider) -> Unit) {
        onReconnectedCallback = callback
    }

    fun onDisconnected(callback: (LyriconProvider) -> Unit) {
        onDisconnectedCallback = callback
    }

    fun onConnectTimeout(callback: (LyriconProvider) -> Unit) {
        onConnectTimeoutCallback = callback
    }

    fun build(): ConnectionListener {
        return ConnectionListenerAdapter(
            onConnected = onConnectedCallback,
            onReconnected = onReconnectedCallback,
            onDisconnected = onDisconnectedCallback,
            onConnectTimeout = onConnectTimeoutCallback
        )
    }
}