package io.github.proify.lyricon.lyric.provider.service

import androidx.annotation.IntRange
import io.github.proify.lyricon.lyric.model.Song

/**
 * 播放器控制接口
 *
 * 提供对远程播放器的控制能力，包括：
 * - 歌曲信息管理
 * - 播放状态控制
 * - 播放进度管理
 * - 文本消息发送
 *
 * 使用示例：
 * ```kotlin
 * // 设置当前播放歌曲
 * player.setSong(Song(title = "Song Title", artist = "Artist"))
 *
 * // 控制播放状态
 * player.setPlaybackState(isPlaying = true)
 *
 * // 更新播放进度（建议每 100-500ms 调用一次）
 * player.updatePosition(position = 5000) // 5 秒
 * ```
 */
interface Player {

    /**
     * 播放器激活状态
     *
     * 判断与远程播放器的连接是否有效。
     * 在执行任何操作前，建议先检查此状态。
     *
     * @return true 表示播放器已连接且进程存活；false 表示未连接或连接已断开
     */
    val isActivated: Boolean

    /**
     * 设置当前播放的歌曲信息
     *
     * 将歌曲元数据同步到远程播放器，用于显示歌词、封面等信息。
     *
     * 注意事项：
     * - 歌曲切换时必须调用此方法更新信息
     * - 传入 null 会清除当前歌曲信息
     * - 建议在歌曲加载完成后立即调用
     *
     * @param song 歌曲对象，包含标题、艺术家、专辑等元数据；
     *             传入 null 表示清除当前歌曲信息
     * @return true 表示命令成功发送；false 表示发送失败（通常是连接已断开）
     */
    fun setSong(song: Song?): Boolean

    /**
     * 设置播放/暂停状态
     *
     * 通知远程播放器当前的播放状态变化。
     * 远程播放器会根据此状态更新 UI 显示（如播放/暂停按钮）。
     *
     * @param isPlaying true 表示正在播放；false 表示已暂停
     * @return true 表示状态更新命令成功发送；false 表示发送失败
     */
    fun setPlaybackState(isPlaying: Boolean): Boolean

    /**
     * 跳转到指定播放位置
     *
     * 主动改变播放进度，例如用户拖动进度条时调用。
     * 此方法会立即更新远程播放器的播放位置。
     *
     * @param positionMs 目标播放位置，单位为毫秒（ms），必须 >= 0
     * @return true 表示跳转命令成功发送；false 表示发送失败
     * @throws IllegalArgumentException 如果 positionMs < 0
     */
    fun seekTo(@IntRange(from = 0) positionMs: Int): Boolean

    /**
     * 更新当前播放位置
     *
     * 定期同步当前播放进度到远程播放器，用于实时更新歌词显示。
     *
     * 使用建议：
     * - 建议通过定时器每 100-500ms 调用一次
     * - 频率过低会导致歌词不同步
     * - 频率过高会增加 IPC 开销
     *
     * 与 [seekTo] 的区别：
     * - `updatePosition`: 正常播放时的进度同步
     * - `seekTo`: 用户主动跳转播放位置
     *
     * @param positionMs 当前播放位置，单位为毫秒（ms），必须 >= 0
     * @return true 表示位置更新命令成功发送；false 表示发送失败
     * @throws IllegalArgumentException 如果 positionMs < 0
     */
    fun updatePosition(@IntRange(from = 0) positionMs: Int): Boolean

    /**
     * 发送文本消息到远程播放器
     *
     * 调用此方法将清除之前设置的Song信息，切换到纯文本模式
     *
     * @param text 要发送的文本内容；
     *             传入 null 表示清除当前显示的文本
     * @return true 表示文本消息成功发送；false 表示发送失败
     */
    fun sendText(text: String?): Boolean
}