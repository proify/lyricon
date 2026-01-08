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
import io.github.proify.lyricon.lyric.model.interfaces.IRichLyricLine
import io.github.proify.lyricon.lyric.model.interfaces.Normalize
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 富歌词
 *
 * @property begin 开始时间
 * @property end 结束时间
 * @property duration 持续时间
 * @property isAlignedRight 是否显示在右边
 * @property metadata 元数据
 * @property text 主文本
 * @property words 主文本单词列表
 * @property secondaryText 次要文本
 * @property secondaryWords 次要文本单词列表
 * @property translationText 翻译文本
 * @property translationWords 翻译文本单词列表
 */
@Serializable
@Parcelize
data class RichLyricLine(
    override var begin: Long = 0,
    override var end: Long = 0,
    override var duration: Long = 0,
    override var isAlignedRight: Boolean = false,
    override var metadata: LyricMetadata? = null,
    override var text: String? = null,
    override var words: List<LyricWord>? = null,
    override var secondaryText: String? = null,
    override var secondaryWords: List<LyricWord>? = null,
    override var translationText: String? = null,
    override var translationWords: List<LyricWord>? = null,
) : IRichLyricLine, Parcelable, DeepCopyable<RichLyricLine>, Normalize<RichLyricLine> {

    override fun deepCopy(): RichLyricLine = copy(
        words = words?.deepCopy(),
        secondaryWords = secondaryWords?.deepCopy(),
        translationWords = translationWords?.deepCopy(),
    )

    override fun normalize(): RichLyricLine = deepCopy().apply {
        words = words?.normalize()
        text = words.toText(text)

        secondaryWords = secondaryWords?.normalize()
        secondaryText = secondaryWords.toText(secondaryText)

        translationWords = translationWords?.normalize()
        translationText = translationWords.toText(translationText)
    }

    private fun List<LyricWord>?.toText(default: String?): String? =
        if (isNullOrEmpty()) default else joinToString("") { it.text.orEmpty() }
}