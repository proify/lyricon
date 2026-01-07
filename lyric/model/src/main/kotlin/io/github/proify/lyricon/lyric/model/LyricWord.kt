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

package io.github.proify.lyricon.lyric.model

import android.os.Parcelable
import io.github.proify.lyricon.lyric.model.interfaces.DeepCopyable
import io.github.proify.lyricon.lyric.model.interfaces.ILyricWord
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 歌词单词
 *
 * @property begin 开始时间
 * @property end 结束时间
 * @property duration 持续时间
 * @property text 文本
 * @property metadata 元数据
 */
@Serializable
@Parcelize
data class LyricWord(
    override var begin: Long = 0,
    override var end: Long = 0,
    override var duration: Long = 0,
    override var text: String? = null,
    override var metadata: LyricMetadata? = null,
) : ILyricWord, Parcelable, DeepCopyable<LyricWord> {

    override fun deepCopy(): LyricWord = copy()
}