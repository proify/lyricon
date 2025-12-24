package io.github.proify.lyricon.lyric.provider

import android.util.Log
import io.github.proify.lyricon.lyric.bridge.provider.IRemoteProviderBinder
import io.github.proify.lyricon.lyric.bridge.provider.IRemoteProviderService
import io.github.proify.lyricon.lyric.bridge.provider.ProviderInfo
import io.github.proify.lyricon.lyric.provider.service.ProviderServiceProxy
import java.util.concurrent.CopyOnWriteArraySet

/**
 * 远程提供者 Binder 实现
 *
 * 作为 AIDL 服务端，处理与订阅者的跨进程通信，包括：
 * - 注册回调通知
 * - 绑定远程服务
 * - 提供提供者信息
 *
 * @property provider 歌词提供者实例
 * @property serviceProxy 提供者服务代理，用于管理远程服务连接
 */
internal class RemoteProviderBinder(
    private val provider: LyriconProvider,
    private val serviceProxy: ProviderServiceProxy
) : IRemoteProviderBinder.Stub() {

    /**
     * 注册回调监听器集合
     *
     * 使用 CopyOnWriteArraySet 保证线程安全的迭代和修改
     */
    private val registrationCallbacks = CopyOnWriteArraySet<RegistrationCallback>()

    /**
     * 注册回调监听器
     *
     * 当远程服务注册成功时会触发回调
     *
     * @param callback 回调监听器
     */
    fun addRegistrationCallback(callback: RegistrationCallback) {
        registrationCallbacks.add(callback)
    }

    /**
     * 取消注册回调监听器
     *
     * @param callback 要移除的回调监听器
     */
    fun removeRegistrationCallback(callback: RegistrationCallback) {
        registrationCallbacks.remove(callback)
    }

    /**
     * 注册回调接口
     *
     * 用于监听远程提供者服务的注册事件
     */
    fun interface RegistrationCallback {
        /**
         * 当远程提供者服务注册成功时调用
         */
        fun onRegistered()
    }

    /**
     * AIDL 方法：处理远程服务注册
     *
     * 当订阅者连接到提供者时调用，负责：
     * 1. 绑定远程提供者服务
     * 2. 通知所有已注册的回调监听器
     *
     * @param remoteProviderService 远程提供者服务接口
     */
    override fun onRegistrationCallback(remoteProviderService: IRemoteProviderService?) {
        // 绑定远程服务到代理
        serviceProxy.bindService(remoteProviderService)

        // 通知所有监听器
        notifyRegistrationCallbacks()
    }

    /**
     * AIDL 方法：获取提供者信息
     *
     * @return 提供者的详细信息（包名、Logo 等）
     */
    override fun getProviderInfo(): ProviderInfo = provider.providerInfo

    /**
     * 通知所有注册的回调监听器
     */
    private fun notifyRegistrationCallbacks() {
        registrationCallbacks.forEach { callback ->
            try {
                callback.onRegistered()
            } catch (e: Exception) {
                Log.e(TAG, "Error in callback", e)
            }
        }
    }

    companion object {
        private const val TAG = "RemoteProviderBinder"
    }
}