/*
 * Copyright 2026 Proify
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.proify.lyricon.provider.remote

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
     * 当前连接状态。
     */
    val connectionStatus: ConnectionStatus

    /**
     * 注册连接状态监听器。
     *
     * @param listener 监听器实例
     * @see ConnectionListener
     */
    fun addConnectionListener(listener: ConnectionListener): Boolean

    /**
     * 移除已注册的连接状态监听器。
     *
     * @param listener 之前注册的监听器实例
     */
    fun removeConnectionListener(listener: ConnectionListener): Boolean
}