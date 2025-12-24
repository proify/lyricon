package io.github.proify.lyricon.lyric.bridge.central.provider.player

import android.util.Log
import io.github.proify.lyricon.lyric.bridge.central.subscriber.OnActivePlayerListener
import io.github.proify.lyricon.lyric.bridge.provider.ProviderInfo
import io.github.proify.lyricon.lyric.model.Song
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * 活跃播放器调度器
 * 负责管理当前活跃的音乐播放器,并将播放事件分发给所有监听器
 *
 * 线程安全:使用读写锁保证并发访问的安全性
 */
object ProviderActivePlayerDispatcher : PlayerListener {
    private const val TAG = "GPAPlayerDispatcher"
    private const val DEBUG = true

    // 事件类型常量
    private object EventType {
        const val SONG_CHANGED = 1
        const val PLAYBACK_STATE_CHANGED = 2
        const val POSITION_CHANGED = 3
        const val SEEK_TO = 4
        const val POST_TEXT = 5
    }

    // 使用读写锁提高并发性能
    private val lock = ReentrantReadWriteLock()
    private var activeProvider = ActiveProvider()
    private val listeners = CopyOnWriteArraySet<OnActivePlayerListener>()

    /**
     * 添加活跃播放器监听器
     */
    fun addOnActivePlayerListener(listener: OnActivePlayerListener) {
        listeners.add(listener)
        if (DEBUG) Log.d(TAG, "Listener added, total: ${listeners.size}")
    }

    /**
     * 移除活跃播放器监听器
     */
    fun removeOnActivePlayerListener(listener: OnActivePlayerListener) {
        listeners.remove(listener)
        if (DEBUG) Log.d(TAG, "Listener removed, total: ${listeners.size}")
    }

    /**
     * 清除所有监听器(用于释放资源)
     */
    fun clearAllListeners() {
        listeners.clear()
        if (DEBUG) Log.d(TAG, "All listeners cleared")
    }

    override fun onSongChanged(recorder: PlayerRecorder, song: Song?) {
        if (DEBUG) Log.d(TAG, "Received onSongChanged: $song")

        handlePlayerEvent(EventType.SONG_CHANGED, recorder) { listener ->
            listener.onSongChanged(song)
        }
    }

    override fun onPlaybackStateChanged(recorder: PlayerRecorder, isPlaying: Boolean) {
        if (DEBUG) Log.d(TAG, "Received onPlaybackStateChanged: $isPlaying")

        handlePlayerEvent(EventType.PLAYBACK_STATE_CHANGED, recorder) { listener ->
            listener.onPlaybackStateChanged(isPlaying)
        }
    }

    override fun onPositionChanged(recorder: PlayerRecorder, position: Int) {
        if (DEBUG) Log.d(TAG, "Received onPositionChanged: $position")

        handlePlayerEvent(EventType.POSITION_CHANGED, recorder) { listener ->
            listener.onPositionChanged(position)
        }
    }

    override fun onSeekTo(recorder: PlayerRecorder, position: Int) {
        if (DEBUG) Log.d(TAG, "Received onSeekTo: $position")

        handlePlayerEvent(EventType.SEEK_TO, recorder) { listener ->
            listener.onSeekTo(position)
        }
    }

    override fun onPostText(recorder: PlayerRecorder, text: String?) {
        if (DEBUG) Log.d(TAG, "Received onPostText: $text")

        handlePlayerEvent(EventType.POST_TEXT, recorder) { listener ->
            listener.onPostText(text)
        }
    }

    /**
     * 处理播放器事件的核心方法
     * 1. 检查并更新活跃播放器
     * 2. 如果需要则广播事件
     */
    private fun handlePlayerEvent(
        eventType: Int,
        recorder: PlayerRecorder,
        notifier: (OnActivePlayerListener) -> Unit
    ) {
        val shouldBroadcast = lock.write {
            val switchResult = checkAndSwitchActiveProvider(eventType, recorder)

            when (switchResult) {
                is SwitchResult.NoSwitch -> {
                    // 同一个播放器,检查是否是活跃的
                    isActiveProvider(recorder.info)
                }

                is SwitchResult.SwitchPerformed -> {
                    // 切换了播放器,同步新播放器的状态
                    syncNewProviderState(recorder)
                    // 如果是歌曲变化事件,不再重复广播(因为同步时已经发送过)
                    eventType != EventType.SONG_CHANGED
                }
            }
        }

        if (shouldBroadcast) {
            broadcastToListeners(notifier)
        } else {
            if (DEBUG) {
                Log.d(
                    TAG, "Event not broadcasted: eventType=$eventType, " +
                            "active=${lock.read { activeProvider.info }}, " +
                            "recorder=${recorder.info}"
                )
            }
        }
    }

    /**
     * 检查并切换活跃播放器
     * @return SwitchResult 表示是否发生了切换
     */
    private fun checkAndSwitchActiveProvider(
        eventType: Int,
        recorder: PlayerRecorder
    ): SwitchResult {
        val recorderInfo = recorder.info
        val current = activeProvider

        // 判断是否需要切换活跃播放器
        val shouldSwitch = when {
            // 情况1: 当前无活跃播放器,且新播放器正在播放
            current.info == null && recorder.isPlaying -> true

            // 情况2: 同一个播放器,更新播放状态
            isSameProvider(current.info, recorderInfo) -> {
                current.isPlaying = recorder.isPlaying
                return SwitchResult.NoSwitch
            }

            // 情况3: 当前播放器已停止,新播放器正在播放
            !current.isPlaying && recorder.isPlaying -> true

            // 其他情况: 不切换
            else -> false
        }

        return if (shouldSwitch && !isSameProvider(current.info, recorderInfo)) {
            // 执行切换
            activeProvider = ActiveProvider(recorderInfo, recorder.isPlaying)
            if (DEBUG) Log.d(TAG, "Active provider switched to: $recorderInfo")

            // 通知所有监听器活跃播放器已改变
            notifyActiveProviderChanged(recorderInfo)

            SwitchResult.SwitchPerformed
        } else {
            SwitchResult.NoSwitch
        }
    }

    /**
     * 同步新活跃播放器的状态
     * 当切换到新的播放器时,需要将该播放器的当前状态同步给所有监听器
     */
    private fun syncNewProviderState(recorder: PlayerRecorder) {
        Log.d(TAG, "Syncing new provider state")

        // 优先发送文本信息(如果有)
        val text = recorder.text
        if (text != null) {
            broadcastToListeners { it.onPostText(text) }
            return
        }

        // 发送歌曲信息
        val song = recorder.song
        if (song != null) {
            broadcastToListeners { it.onSongChanged(song) }
        }

        // 发送播放位置
        val lastPosition = recorder.lastPosition
        if (lastPosition > 0) {
            broadcastToListeners { it.onSeekTo(lastPosition) }
        }
    }

    /**
     * 通知活跃播放器已改变
     */
    private fun notifyActiveProviderChanged(info: ProviderInfo) {
        broadcastToListeners { it.onActiveProviderChanged(info) }
    }

    /**
     * 向所有监听器广播事件
     * 添加了异常处理,防止单个监听器的异常影响其他监听器
     */
    private fun broadcastToListeners(notifier: (OnActivePlayerListener) -> Unit) {
        if (DEBUG) Log.d(TAG, "Broadcasting to ${listeners.size} listeners")

        listeners.forEach { listener ->
            try {
                notifier(listener)
            } catch (e: Exception) {
                Log.e(TAG, "Error notifying listener: ${listener.javaClass.simpleName}", e)
            }
        }
    }

    /**
     * 检查指定的 ProviderInfo 是否是当前活跃的播放器
     */
    private fun isActiveProvider(info: ProviderInfo?): Boolean {
        return isSameProvider(activeProvider.info, info)
    }

    /**
     * 判断两个 ProviderInfo 是否表示同一个播放器
     */
    private fun isSameProvider(a: ProviderInfo?, b: ProviderInfo?): Boolean {
        if (a == null || b == null) return false
        return a.modulePackageName == b.modulePackageName &&
                a.musicAppPackageName == b.musicAppPackageName
    }

    /**
     * 活跃播放器数据类
     */
    private data class ActiveProvider(
        var info: ProviderInfo? = null,
        var isPlaying: Boolean = false
    )

    /**
     * 播放器切换结果
     */
    private sealed class SwitchResult {
        /** 未发生切换 */
        object NoSwitch : SwitchResult()

        /** 已执行切换 */
        object SwitchPerformed : SwitchResult()
    }
}