/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

@file:Suppress("unused")

package io.github.proify.lyricon.xposed.systemui.util

import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.github.proify.lyricon.xposed.systemui.util.SystemUIMediaUtils.updateCallbackRegistrations
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

/**
 *  **系统级媒体监听工具类**
 *
 * 该类通过 [MediaSessionManager] 实现对系统活动媒体会话的全局监听。
 *
 * @author Tomakino
 * @since 2026-04-13
 */
object SystemUIMediaUtils {

    /** 日志标识 */
    private const val TAG: String = "SystemUIMediaHooker"

    /**
     * 活跃会话容器
     * 使用 [ConcurrentHashMap] 存储 [MediaSession.Token] 与对应包装器的映射，确保多线程环境下会话管理的原子性。
     */
    private val activeSessions: ConcurrentHashMap<MediaSession.Token, ControllerWrapper> =
        ConcurrentHashMap<MediaSession.Token, ControllerWrapper>()

    /** * 外部监听器集合
     * 使用 [CopyOnWriteArraySet] 存储回调，支持在遍历过程中进行并发修改（注册/注销）。
     */
    private val listeners: CopyOnWriteArraySet<MediaControllerCallback> =
        CopyOnWriteArraySet<MediaControllerCallback>()

    /** 系统媒体会话管理器引用 */
    private var mediaSessionManager: MediaSessionManager? = null

    /** 确保回调在主线程分发的 Handler 实例 */
    private val mainHandler: Handler = Handler(Looper.getMainLooper())

    /**
     * 会话状态变更监听器
     * 当系统内有新的媒体会话启动或现有会话关闭时，触发 [updateCallbackRegistrations]。
     */
    private val sessionListener: MediaSessionManager.OnActiveSessionsChangedListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers: List<MediaController>? ->
            updateCallbackRegistrations(controllers)
        }

    /**
     * 初始化工具类
     * @param context 建议传入 ApplicationContext 以防止内存泄漏。
     */
    fun init(context: Context) {
        if (mediaSessionManager != null) return

        val appContext: Context = context.applicationContext
        val manager: MediaSessionManager? =
            appContext.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
        mediaSessionManager = manager

        try {
            manager?.let {
                // 注册系统活跃会话变更回调
                it.addOnActiveSessionsChangedListener(sessionListener, null)
                // 执行初次全量会话同步
                updateCallbackRegistrations(it.getActiveSessions(null))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SystemUIMediaHooker", e)
        }
    }

    /**
     * 同步并更新当前所有媒体控制器的回调注册状态
     *  @param controllers 当前系统中最新的活跃控制器列表
     */
    private fun updateCallbackRegistrations(controllers: List<MediaController>?) {
        // 提取最新 Token 集合用于比对差集
        val newTokens: Set<MediaSession.Token> =
            controllers?.map { it.sessionToken }?.toSet() ?: emptySet()

        // 1. 移除并销毁已失效的会话
        val iterator = activeSessions.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (!newTokens.contains(entry.key)) {
                entry.value.release()
                iterator.remove()
            }
        }

        // 2. 注册新增的会话
        controllers?.forEach { controller: MediaController ->
            val token: MediaSession.Token = controller.sessionToken
            if (!activeSessions.containsKey(token)) {
                val wrapper = ControllerWrapper(controller)
                wrapper.register()
                activeSessions[token] = wrapper
                // 立即派发当前已存在的状态
                dispatchInitialState(controller)
            }
        }
    }

    /**
     * 针对新识别的控制器，派发其当前的元数据和播放状态
     * * @param controller 目标媒体控制器
     */
    private fun dispatchInitialState(controller: MediaController) {
        val metadata: MediaMetadata? = controller.metadata
        val state: PlaybackState? = controller.playbackState
        mainHandler.post {
            listeners.forEach { listener: MediaControllerCallback ->
                metadata?.let { listener.onMediaChanged(controller, it) }
                state?.let { listener.onPlaybackStateChanged(controller, it) }
            }
        }
    }

    /**
     * 媒体控制器包装器类
     * 封装了对 [MediaController.Callback] 的内部实现，负责管理特定会话的生命周期。
     *  @property controller 原始媒体控制器实例
     */
    private class ControllerWrapper(internal val controller: MediaController) {

        /** 所属应用包名 */
        val packageName: String = controller.packageName ?: "unknown"

        /**
         * 媒体状态变化回调实现
         * 拦截并转发元数据、播放状态及销毁事件至全局监听器。
         */
        private val callback: MediaController.Callback = object : MediaController.Callback() {
            override fun onMetadataChanged(metadata: MediaMetadata?) {
                metadata?.let { m: MediaMetadata ->
                    mainHandler.post {
                        listeners.forEach { it.onMediaChanged(controller, m) }
                    }
                }
            }

            override fun onPlaybackStateChanged(state: PlaybackState?) {
                state?.let { s: PlaybackState ->
                    mainHandler.post {
                        listeners.forEach { it.onPlaybackStateChanged(controller, s) }
                    }
                }
            }

            override fun onSessionDestroyed() {
                mainHandler.post {
                    // 会话销毁时从容器中移除并解绑
                    activeSessions.remove(controller.sessionToken)?.release()
                    listeners.forEach { it.onSessionDestroyed(controller) }
                }
            }
        }

        /**
         * 向系统注册回调
         */
        fun register() {
            try {
                controller.registerCallback(callback, mainHandler)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register callback for $packageName", e)
            }
        }

        /**
         * 解绑回调，释放资源
         */
        fun release() {
            try {
                controller.unregisterCallback(callback)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unregister callback for $packageName", e)
            }
        }
    }

    /**
     * 注册自定义媒体回调
     * @return 如果注册成功返回 true
     */
    fun registerListener(l: MediaControllerCallback): Boolean = listeners.add(l)

    /**
     * 注销自定义媒体回调
     * @return 如果注销成功返回 true
     */
    fun unregisterListener(l: MediaControllerCallback): Boolean = listeners.remove(l)

    /**
     * 获取指定包名对应的活跃媒体控制器。
     *
     * 用于"状态栏歌词控制窗口"等场景，向当前播放器下发播放/暂停、上一首/下一首、跳转进度等指令。
     *
     * @param packageName 目标播放器应用包名，传 null 时返回最近注册的控制器
     * @return 匹配到的 [MediaController]，未找到时返回 null
     */
    fun getController(packageName: String? = null): MediaController? {
        if (activeSessions.isEmpty()) return null
        val target = packageName?.takeIf { it.isNotBlank() }
        if (target != null) {
            return activeSessions.values.firstOrNull { it.packageName == target }?.controller
        }
        // 返回最近通过 callback 注册的控制器作为兜底
        return activeSessions.values.lastOrNull()?.controller
    }

    /**
     * 媒体控制器事件回调接口
     */
    interface MediaControllerCallback {
        /** 当歌曲信息（标题、艺人、封面等）更新时触发 */
        fun onMediaChanged(controller: MediaController, metadata: MediaMetadata)

        /** 当播放状态（播放/暂停/进度）变更时触发，默认空实现 */
        fun onPlaybackStateChanged(controller: MediaController, state: PlaybackState) {}

        /** 当该媒体会话被系统彻底关闭时触发 */
        fun onSessionDestroyed(controller: MediaController){}
    }
}