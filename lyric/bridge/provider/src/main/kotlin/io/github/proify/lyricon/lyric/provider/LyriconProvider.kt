package io.github.proify.lyricon.lyric.provider

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import io.github.proify.lyricon.lyric.bridge.core.Constants
import io.github.proify.lyricon.lyric.bridge.provider.ProviderInfo
import io.github.proify.lyricon.lyric.bridge.provider.ProviderLogo
import io.github.proify.lyricon.lyric.provider.service.ConnectionStatus
import io.github.proify.lyricon.lyric.provider.service.ProviderService
import io.github.proify.lyricon.lyric.provider.service.ProviderServiceImpl
import java.util.Timer
import java.util.TimerTask

/**
 * 歌词提供者核心类
 *
 * 负责将歌词源注册到中心服务，并管理与中心服务的连接状态。
 * 提供者可以为特定的音乐应用提供歌词数据。
 *
 * 使用示例：
 * ```kotlin
 * val provider = LyriconProvider(
 *     context = context,
 *     modulePackageName = "com.example.lyricon.provider.spotify",
 *     musicAppPackageName = "com.spotify.music",
 *     logo = ProviderLogo.fromDrawable(drawable)
 * )
 * provider.register()
 * ```
 *
 * @param context Android 上下文
 * @param modulePackageName 提供者模块的包名
 * @param musicAppPackageName 目标音乐应用的包名
 * @param logo 音乐应用的 Logo，可选
 * @param metadata 提供者的扩展元数据
 */
class LyriconProvider(
    context: Context,
    modulePackageName: String,
    musicAppPackageName: String,
    logo: ProviderLogo? = null,
    metadata: Bundle = Bundle.EMPTY
) {

    internal companion object {
        private const val TAG = "Provider"
        private const val CONNECTION_TIMEOUT_MS = 2233L
    }

    private val appContext: Context = context.applicationContext

    /**
     * 提供者信息
     *
     * 包含模块包名、目标应用包名、Logo 等基本信息
     */
    val providerInfo: ProviderInfo = ProviderInfo(
        modulePackageName = modulePackageName,
        musicAppPackageName = musicAppPackageName,
        logo = logo,
        metadata = metadata
    )

    private val serviceImpl = ProviderServiceImpl(this)
    private val binder = RemoteProviderBinder(this, serviceImpl)

    /**
     * 提供者服务接口
     *
     * 用于管理歌词数据推送和连接状态监听
     */
    val service: ProviderService = serviceImpl

    /**
     * 服务激活状态
     *
     * @return true 表示服务已激活并可以推送歌词
     */
    val isActivated: Boolean
        get() = service.isActivated

    /** 连接超时定时器 */
    private var connectionTimeoutTimer: Timer? = null

    /** 中心服务状态监听器 */
    private val centralServiceListener = object : CentralServiceReceiver.ServiceListener {
        override fun onServiceBootCompleted() {
            if (serviceImpl.connectionStatus == ConnectionStatus.STATUS_DISCONNECTED) {
                Log.d(TAG, "Central service rebooted, re-registering provider")
                register()
            }
        }
    }

    private var destoryed = false

    init {
        initializeCentralServiceReceiver()
    }

    /**
     * 初始化中心服务接收器
     */
    private fun initializeCentralServiceReceiver() {
        if (!CentralServiceReceiver.initialized) {
            CentralServiceReceiver.initialize(appContext)
        }
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
        return when (serviceImpl.connectionStatus) {
            ConnectionStatus.STATUS_CONNECTED -> {
                Log.d(TAG, "Provider already registered")
                false
            }

            ConnectionStatus.STATUS_CONNECTING -> {
                Log.d(TAG, "Provider registration already in progress")
                false
            }

            else -> {
                Log.d(TAG, "Registering provider: ${providerInfo.modulePackageName}")
                performRegistration()
            }
        }
    }

    /**
     * 执行实际的注册流程
     */
    private fun performRegistration(): Boolean {
        return try {
            serviceImpl.connectionStatus = ConnectionStatus.STATUS_CONNECTING

            startConnectionTimeout()
            setupRegistrationCallback()
            sendRegistrationBroadcast()

            Log.d(TAG, "Provider registration broadcast sent")
            true
        } catch (e: Exception) {
            handleRegistrationError(e)
            false
        }
    }

    /**
     * 启动连接超时计时器
     */
    private fun startConnectionTimeout() {
        connectionTimeoutTimer?.cancel()
        connectionTimeoutTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    handleConnectionTimeout()
                }
            }, CONNECTION_TIMEOUT_MS)
        }
    }

    /**
     * 处理连接超时
     */
    private fun handleConnectionTimeout() {
        if (serviceImpl.connectionStatus == ConnectionStatus.STATUS_CONNECTING) {
            serviceImpl.connectionStatus = ConnectionStatus.STATUS_DISCONNECTED

            Log.w(TAG, "Provider connection timeout")
            serviceImpl.connectionListeners.forEach { listener ->
                try {
                    listener.onConnectTimeout(this@LyriconProvider)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in connection timeout listener", e)
                }
            }
        }
    }

    /**
     * 设置注册成功回调
     */
    private fun setupRegistrationCallback() {
        binder.addRegistrationCallback {
            connectionTimeoutTimer?.cancel()
            serviceImpl.connectionStatus = ConnectionStatus.STATUS_CONNECTED
            Log.d(TAG, "Provider registered successfully")
        }
    }

    /**
     * 发送注册广播
     */
    private fun sendRegistrationBroadcast() {
        val bundle = Bundle().apply {
            putBinder(Constants.EXTRA_BINDER, binder)
        }

        val intent = Intent(Constants.ACTION_REGISTER_PROVIDER).apply {
            setPackage(Constants.CENTRAL_PACKAGE_NAME)
            putExtra(Constants.EXTRA_BUNDLE, bundle)
        }

        appContext.sendBroadcast(intent)
    }

    /**
     * 处理注册错误
     */
    private fun handleRegistrationError(e: Exception) {
        serviceImpl.connectionStatus = ConnectionStatus.STATUS_DISCONNECTED
        connectionTimeoutTimer?.cancel()
        Log.e(TAG, "Failed to register provider", e)
    }

    /**
     * 取消注册提供者
     *
     * 断开与中心服务的连接，停止歌词推送。
     * 由用户主动调用。
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

    /**
     * 内部取消注册方法
     *
     * @param fromUser true 表示用户主动取消，false 表示系统自动取消
     *
     */
    private fun unregisterInternal(fromUser: Boolean) {
        try {
            connectionTimeoutTimer?.cancel()
            serviceImpl.disconnect(fromUser)
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
        connectionTimeoutTimer?.cancel()
        connectionTimeoutTimer = null
    }
}