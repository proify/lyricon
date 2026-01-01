/*
 * Lyricon – An Xposed module that extends system functionality
 * Copyright (C) 2026 Proify
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.proify.lyricon.provider

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import io.github.proify.lyricon.provider.ProviderBinder.RegistrationCallback
import io.github.proify.lyricon.provider.remote.ConnectionStatus
import io.github.proify.lyricon.provider.remote.RemoteService
import io.github.proify.lyricon.provider.remote.RemoteServiceProxy
import java.util.Timer
import java.util.TimerTask

/**
 * 歌词提供者核心类1
 *
 * 负责将歌词源注册到中心服务，并管理与中心服务的连接状态。
 * 提供者可以为特定的音乐应用提供歌词数据。
 *
 * 使用示例：
 * ```kotlin
 * val provider = LyriconProvider(
 *     context = context,
 *     modulePackageName = "com.example.lyricon.provider.spotify",
 *     musicPackageName = "com.spotify.music",
 *     logo = ProviderLogo.fromDrawable(drawable)
 * )
 * provider.register()
 * ```
 *
 * @param context 上下文
 * @param providerPackageName 提供者模块的包名
 * @param playerPackageName 目标音乐应用的包名
 * @param logo 音乐应用的 Logo
 * @param extraMetadata 扩展元数据
 */
class LyriconProvider(
    context: Context,
    providerPackageName: String,
    playerPackageName: String,
    logo: ProviderLogo? = null,
    extraMetadata: Map<String, String?> = emptyMap()
) {

    internal companion object {
        private const val TAG = "LyriconProvider"
        private const val CONNECTION_TIMEOUT_MS = 2233L
    }

    private val appContext: Context = context.applicationContext

    val providerInfo: ProviderInfo = ProviderInfo(
        providerPackageName = providerPackageName,
        playerPackageName = playerPackageName,
        logo = logo,
        extraMetadata = extraMetadata
    )

    private val providerService = ProviderService()
    private val remoteService = RemoteServiceProxy(this)
    private val binder = ProviderBinder(this, providerService, remoteService)

    /**
     * 远程服务
     */
    val service: RemoteService = remoteService

    /**
     * 服务激活状态
     */
    val isActivated: Boolean
        get() = service.isActivated

    private var connectionTimeoutTimer: Timer? = null

    private val centralServiceListener = object : CentralServiceReceiver.ServiceListener {
        override fun onServiceBootCompleted() {
            if (remoteService.connectionStatus == ConnectionStatus.STATUS_DISCONNECTED) {
                Log.d(TAG, "Central service rebooted, re-registering provider")
                register()
            }
        }
    }

    private var destoryed = false

    init {
        initializeCentralServiceReceiver()
    }

    private fun initializeCentralServiceReceiver() {
        CentralServiceReceiver.initialize(appContext)
        CentralServiceReceiver.addServiceListener(centralServiceListener)
    }

    /**
     * 注册提供者到中心服务
     *
     * 发送注册广播到中心服务，建立跨进程连接。
     * 如果已经处于连接或连接中状态，则不会重复注册。
     *
     * @return true 表示注册请求已发送；false 表示已连接或正在连接中
     *
     * @throws IllegalStateException 如果提供者已被销毁
     */
    fun register(): Boolean {
        if (destoryed) {
            throw IllegalStateException("Provider has been destroyed")
            return false
        }
        return when (remoteService.connectionStatus) {
            ConnectionStatus.STATUS_CONNECTED -> {
                Log.d(TAG, "Provider already registered")
                false
            }

            ConnectionStatus.STATUS_CONNECTING -> {
                Log.d(TAG, "Provider registration already in progress")
                false
            }

            else -> {
                Log.d(TAG, "Registering provider: ${providerInfo.providerPackageName}")
                performRegistration()
                return true
            }
        }
    }

    /**
     * 执行实际的注册流程
     */
    private fun performRegistration() {

        val registrationCallback = object : RegistrationCallback {
            override fun onRegistered() {
                connectionTimeoutTimer?.cancel()
                connectionTimeoutTimer = null

                remoteService.connectionStatus = ConnectionStatus.STATUS_CONNECTED
                binder.removeRegistrationCallback(this)

                Log.d(TAG, "Provider registered successfully")
            }
        }

        remoteService.connectionStatus = ConnectionStatus.STATUS_CONNECTING

        connectionTimeoutTimer?.cancel()
        connectionTimeoutTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    if (remoteService.connectionStatus == ConnectionStatus.STATUS_CONNECTING) {
                        remoteService.connectionStatus = ConnectionStatus.STATUS_DISCONNECTED

                        binder.removeRegistrationCallback(registrationCallback)

                        Log.w(TAG, "Provider connection timeout")
                        remoteService.connectionListeners.forEach { listener ->
                            try {
                                listener.onConnectTimeout(this@LyriconProvider)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error in connection timeout listener", e)
                            }
                        }
                    }
                }
            }, CONNECTION_TIMEOUT_MS)
        }

        binder.addRegistrationCallback(registrationCallback)

        val bundle = Bundle().apply {
            putBinder(Constants.EXTRA_BINDER, binder)
        }

        val intent = Intent(Constants.ACTION_REGISTER_PROVIDER).apply {
            setPackage(Constants.CENTRAL_PACKAGE_NAME)
            putExtra(Constants.EXTRA_BUNDLE, bundle)
        }

        appContext.sendBroadcast(intent)

        Log.d(TAG, "Provider registration broadcast sent")
    }

    /**
     * 取消注册提供者
     *
     * 断开与中心服务的连接
     *
     * @throws IllegalStateException 如果提供者已被销毁
     */
    fun unregister() {
        if (destoryed) {
            throw IllegalStateException("Provider has been destroyed")
            return
        }
        unregisterInternal(fromUser = true)
    }

    private fun unregisterInternal(fromUser: Boolean) {
        try {
            connectionTimeoutTimer?.cancel()
            connectionTimeoutTimer = null

            remoteService.disconnect(fromUser)
            Log.d(TAG, "Provider unregistered ${if (fromUser) "by user" else "automatically"}")
        } catch (e: Exception) {
            Log.e(TAG, "Error during unregister", e)
        }
    }

    /**
     * 清理资源
     *
     * 在提供者不再使用时调用，释放相关资源
     */
    fun destory() {
        if (destoryed) return
        destoryed = true
        unregisterInternal(fromUser = false)
        CentralServiceReceiver.removeServiceListener(centralServiceListener)
    }
}