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

package io.github.proify.lyricon.provider.remote

import androidx.annotation.IntRange
import io.github.proify.lyricon.lyric.model.Song

interface RemotePlayer {

    /**
     * 判断与远程播放器的连接是否有效。
     */
    val isActivated: Boolean

    /**
     * 设置当前播放的歌曲信息
     *
     * @param song 歌曲对象
     *             传入 null 表示清除当前歌曲信息
     * @return 命令是否成功发送
     */
    fun setSong(song: Song?): Boolean

    /**
     * 设置播放/暂停状态
     *
     * @param isPlaying 是否播放中
     * @return 命令是否成功发送
     */
    fun setPlaybackState(isPlaying: Boolean): Boolean

    /**
     * 立即跳转到指定播放位置
     *
     * 主动改变播放进度，例如用户拖动进度条时调用。
     *
     * @param position 播放位置
     */
    fun seekTo(@IntRange(from = 0) position: Int): Boolean

    /**
     * 将播放位置写入到内存待同步区，根据轮询间隔读取并同步。
     **
     * @param position 播放位置
     * @see setPositionUpdateInterval
     */
    fun setPosition(@IntRange(from = 0) position: Int): Boolean

    /**
     * 设置轮询间隔，过高会导致歌词延迟问题，过低会导致性能下降，建议控制在100-200ms之间
     */
    fun setPositionUpdateInterval(@IntRange(from = 0) interval: Int): Boolean

    /**
     * 发送文本消息到远程播放器
     *
     * **调用此方法将清除之前设置的Song信息，切换到纯文本模式**
     *
     * @param text 要发送的文本内容；
     *             传入 null 表示清除当前显示的文本
     * @return 命令是否成功发送
     */
    fun sendText(text: String?): Boolean
}