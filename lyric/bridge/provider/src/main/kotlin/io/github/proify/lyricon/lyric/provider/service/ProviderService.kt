package io.github.proify.lyricon.lyric.provider.service

/**
 * 提供者服务接口
 *
 * 管理歌词提供者与中心服务之间的连接，提供以下核心功能：
 * - 播放器控制：通过 [player] 控制远程播放器
 * - 连接管理：连接状态查询和断开操作
 * - 状态监听：注册监听器接收连接状态变化通知
 *
 * 使用示例：
 * ```kotlin
 * val service = provider.service
 *
 * // 检查服务状态
 * if (service.isActivated) {
 *     // 控制播放器
 *     service.player.setSong(song)
 *     service.player.setPlaybackState(true)
 * }
 *
 * // 监听连接状态
 * service.addConnectionListener {
 *     onConnected { provider ->
 *         // 服务已连接
 *     }
 *     onReconnected { provider ->
 *         // 服务已重连
 *     }
 *     onDisconnected { provider ->
 *         // 服务已断开
 *     }
 * }
 * ```
 */
interface ProviderService {

    /**
     * 远程播放器控制接口
     *
     * 用于控制中心服务的播放器，包括设置歌曲信息、
     * 播放状态、播放进度等操作。
     *
     * 注意：在调用 player 的方法前，建议先检查 [isActivated] 状态。
     */
    val player: Player

    /**
     * 服务激活状态
     *
     * 表示提供者服务是否已成功连接到中心服务且可用。
     *
     * @return true 表示服务已激活，可以正常使用；
     *         false 表示服务未连接或连接已断开
     */
    val isActivated: Boolean

    /**
     * 当前连接状态
     *
     * 返回详细的连接状态码，用于精确判断当前连接状况。
     *
     * 可能的状态值：
     * - [ConnectionStatus.STATUS_DISCONNECTED] - 未连接
     * - [ConnectionStatus.STATUS_DISCONNECTED_BY_USER] - 用户主动断开
     * - [ConnectionStatus.STATUS_CONNECTING] - 连接中
     * - [ConnectionStatus.STATUS_CONNECTED] - 已连接
     *
     * @return 连接状态码
     */
    val connectionStatus: ConnectionStatus

    /**
     * 注册连接状态监听器
     *
     * 添加一个监听器以接收连接状态变化的通知。
     * 监听器会在连接、断开、重连、超时等事件发生时被回调。
     *
     * 注意事项：
     * - 同一个监听器对象只能注册一次
     * - 监听器会被强引用，使用完毕后应调用 [removeConnectionListener] 移除
     * - 回调在主线程或 Binder 线程执行，注意线程安全
     *
     * @param listener 连接状态监听器
     */
    fun addConnectionListener(listener: ConnectionListener)

    /**
     * DSL 方式创建连接监听器
     *
     * 使用示例：
     * ```kotlin
     * addConnectionListener {
     *             onConnected { provider ->
     *
     *             }
     *             onReconnected { provider ->
     *
     *             }
     *             onDisconnected { provider ->
     *
     *             }
     *         }
     * ```
     */
    fun addConnectionListener(builder: ConnectionListenerBuilder.() -> Unit): ConnectionListener

    /**
     * 移除连接状态监听器
     *
     * 取消注册之前添加的监听器，停止接收状态变化通知。
     *
     * 注意事项：
     * - 如果监听器未注册，此方法不会产生任何效果
     * - 建议在不再需要监听时及时移除，避免内存泄漏
     *
     * @param listener 要移除的监听器
     */
    fun removeConnectionListener(listener: ConnectionListener)
}

/**
 * 提供者服务扩展函数
 */

/**
 * 检查服务是否已连接
 *
 * @return true 表示已连接到中心服务
 */
fun ProviderService.isConnected(): Boolean {
    return connectionStatus == ConnectionStatus.STATUS_CONNECTED
}

/**
 * 检查服务是否正在连接中
 *
 * @return true 表示正在尝试连接到中心服务
 */
fun ProviderService.isConnecting(): Boolean {
    return connectionStatus == ConnectionStatus.STATUS_CONNECTING
}

/**
 * 检查服务是否已断开
 *
 * @return true 表示未连接或已断开连接
 */
fun ProviderService.isDisconnected(): Boolean {
    return connectionStatus == ConnectionStatus.STATUS_DISCONNECTED ||
            connectionStatus == ConnectionStatus.STATUS_DISCONNECTED_BY_USER
}

/**
 * 检查是否由用户主动断开连接
 *
 * @return true 表示由用户主动断开连接
 */
fun ProviderService.isDisconnectedByUser(): Boolean {
    return connectionStatus == ConnectionStatus.STATUS_DISCONNECTED_BY_USER
}