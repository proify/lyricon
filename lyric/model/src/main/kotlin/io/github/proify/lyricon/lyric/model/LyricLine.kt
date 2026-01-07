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

package io.github.proify.lyricon.lyric.model

import android.os.Parcelable
import io.github.proify.lyricon.lyric.model.extensions.deepCopy
import io.github.proify.lyricon.lyric.model.extensions.normalize
import io.github.proify.lyricon.lyric.model.interfaces.DeepCopyable
import io.github.proify.lyricon.lyric.model.interfaces.ILyricLine
import io.github.proify.lyricon.lyric.model.interfaces.Normalize
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 歌词行
 *
 * @property begin 开始时间
 * @property end 结束时间
 * @property duration 持续时间
 * @property isAlignedRight 是否显示在右边
 * @property metadata 元数据
 * @property text 文本
 * @property words 文本单词列表
 */
@Serializable
@Parcelize
data class LyricLine(
    override var begin: Long = 0,
    override var end: Long = 0,
    override var duration: Long = 0,
    override var isAlignedRight: Boolean = false,
    override var metadata: LyricMetadata? = null,
    override var text: String? = null,
    override var words: List<LyricWord>? = null,
) : ILyricLine, Parcelable, DeepCopyable<LyricLine>, Normalize<LyricLine> {

    override fun deepCopy(): LyricLine = copy(
        words = words?.deepCopy()
    )

    override fun normalize(): LyricLine = deepCopy().apply {
        words = words?.normalize()
        text = words
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString("") { it.text.orEmpty() }
            ?: text
    }
}