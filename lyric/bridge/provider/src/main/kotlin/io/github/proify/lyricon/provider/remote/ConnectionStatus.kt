package io.github.proify.lyricon.provider.remote

enum class ConnectionStatus {
    /**
     * 未连接状态
     */
    STATUS_DISCONNECTED,

    /**
     *  已断开连接（用户主动触发）
     */
    STATUS_DISCONNECTED_BY_USER,

    /**
     *  连接中状态
     */
    STATUS_CONNECTING,

    /**
     * 已连接状态
     */
    STATUS_CONNECTED,
}