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

import io.github.proify.lyricon.provider.LyriconProvider

/**
 * 中央服务连接状态监听器。
 */
interface ConnectionListener {

    /**
     * 当提供者与中心服务首次成功建立连接时回调。
     *
     * @param provider 触发回调的提供者实例
     */
    fun onConnected(provider: LyriconProvider)

    /**
     * 当提供者在断开后重新建立连接时回调。
     *
     * @param provider 触发回调的提供者实例
     */
    fun onReconnected(provider: LyriconProvider)

    /**
     * 当提供者与中心服务连接断开时回调。
     *
     * @param provider 触发回调的提供者实例
     */
    fun onDisconnected(provider: LyriconProvider)

    /**
     * 当提供者在规定时间内未能完成连接注册时回调。
     *
     * @param provider 触发回调的提供者实例
     */
    fun onConnectTimeout(provider: LyriconProvider)
}