package io.github.proify.lyricon.provider

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import io.github.proify.lyricon.provider.ProviderBinder.OnRegistrationCallback
import io.github.proify.lyricon.provider.remote.ConnectionStatus
import io.github.proify.lyricon.provider.remote.RemoteService
import io.github.proify.lyricon.provider.remote.RemoteServiceProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 歌词提供者类
 *
 * @property providerInfo 提供者信息
 * @property service 对外暴露的远程服务接口
 * @property isActivated 是否处于激活状态
 */
class LyriconProvider(
    context: Context,
    providerPackageName: String,
    playerPackageName: String,
    logo: ProviderLogo? = null,
    metadata: ProviderMetadata? = null
) {

    companion object {
        private const val TAG = "LyriconProvider"

        /** 注册等待超时时间 */
        private const val CONNECTION_TIMEOUT_MS = 2_233L
    }

    private val appContext: Context = context.applicationContext
    private val providerService = ProviderService()
    private val remoteServiceProxy = RemoteServiceProxy(this)
    private val binder = ProviderBinder(this, providerService, remoteServiceProxy)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var connectionTimeoutJob: Job? = null
    private val destroyed = AtomicBoolean(false)

    /** 监听中心服务生命周期变化 */
    private val centralServiceListener = object : CentralServiceReceiver.ServiceListener {
        override fun onServiceBootCompleted() {
            if (remoteServiceProxy.connectionStatus == ConnectionStatus.STATUS_DISCONNECTED) {
                Log.d(TAG, "Central service restarted, attempting re-registration")
                register()
            }
        }
    }

    /** 提供者信息 */
    val providerInfo: ProviderInfo = ProviderInfo(
        providerPackageName = providerPackageName,
        playerPackageName = playerPackageName,
        logo = logo,
        metadata = metadata
    )

    /** 远程服务接口 */
    val service: RemoteService = remoteServiceProxy

    /** 远程服务是否已激活 */
    val isActivated: Boolean
        get() = service.isActivated

    init {
        CentralServiceReceiver.initialize(appContext)
        CentralServiceReceiver.addServiceListener(centralServiceListener)
    }

    /**
     * 向中心服务发起注册请求。
     *
     * @return true 表示已成功发起注册流程，false 表示已连接或连接中
     * @throws IllegalStateException 当实例已被销毁时
     */
    @Synchronized
    fun register(): Boolean {
        if (destroyed.get()) {
            throw IllegalStateException("Provider has been destroyed")
        }

        return when (remoteServiceProxy.connectionStatus) {
            ConnectionStatus.STATUS_CONNECTED,
            ConnectionStatus.STATUS_CONNECTING -> false

            else -> {
                performRegistration()
                true
            }
        }
    }

    /**
     * 执行注册流程：
     * - 设置连接状态为连接中
     * - 启动连接超时任务
     * - 向中心服务发送注册广播
     */
    private fun performRegistration() {

        connectionTimeoutJob?.cancel()
        connectionTimeoutJob = null

        val registrationCallback = object : OnRegistrationCallback {
            override fun onRegistered() {
                connectionTimeoutJob?.cancel()
                connectionTimeoutJob = null
                remoteServiceProxy.connectionStatus = ConnectionStatus.STATUS_CONNECTED
                binder.removeRegistrationCallback(this)
            }
        }

        remoteServiceProxy.connectionStatus = ConnectionStatus.STATUS_CONNECTING

        connectionTimeoutJob = scope.launch {
            delay(CONNECTION_TIMEOUT_MS)
            if (remoteServiceProxy.connectionStatus == ConnectionStatus.STATUS_CONNECTING) {
                remoteServiceProxy.connectionStatus = ConnectionStatus.STATUS_DISCONNECTED
                binder.removeRegistrationCallback(registrationCallback)

                remoteServiceProxy.connectionListeners.forEach {
                    runCatching {
                        it.onConnectTimeout(this@LyriconProvider)
                    }
                }
            }
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
    }

    /**
     * 注销提供者。
     *
     * @throws IllegalStateException 当实例已被销毁时
     */
    @Synchronized
    fun unregister() {
        if (destroyed.get()) {
            throw IllegalStateException("Provider has been destroyed")
        }
        unregisterInternal(fromUser = true)
    }

    /**
     * 注销实现逻辑，可区分用户主动调用或系统自动调用
     *
     * @param fromUser 是否由用户触发
     */
    private fun unregisterInternal(fromUser: Boolean) {
        connectionTimeoutJob?.cancel()
        connectionTimeoutJob = null
        remoteServiceProxy.disconnect(fromUser)
    }

    /**
     * 释放资源并结束实例生命周期。
     *
     * 调用后该对象不可再次使用。
     */
    fun destroy() {
        if (!destroyed.compareAndSet(false, true)) return

        scope.cancel()
        unregisterInternal(fromUser = false)
        CentralServiceReceiver.removeServiceListener(centralServiceListener)
    }
}