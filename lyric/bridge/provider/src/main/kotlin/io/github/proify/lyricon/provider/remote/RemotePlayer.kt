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
    fun seekTo(@IntRange(from = 0) position: Long): Boolean

    /**
     * 将播放位置写入到待同步区，根据轮询间隔读取并同步。
     **
     * @param position 播放位置
     * @see setPositionUpdateInterval
     */
    fun setPosition(@IntRange(from = 0) position: Long): Boolean

    /**
     * 设置轮询间隔
     */
    fun setPositionUpdateInterval(@IntRange(from = 0) interval: Int): Boolean

    /**
     * 发送文本消息到远程播放器
     *
     * **调用此方法将清除之前设置的Song信息，切换到纯文本模式**
     *
     * @param text 要发送的文本内容；
     * @return 命令是否成功发送
     */
    fun sendText(text: String?): Boolean
}