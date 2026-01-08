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

@file:Suppress("MemberVisibilityCanBePrivate")

package io.github.proify.lyricon.lyric.model

import android.os.Parcelable
import io.github.proify.lyricon.lyric.model.extensions.deepCopy
import io.github.proify.lyricon.lyric.model.extensions.normalizeSortByTime
import io.github.proify.lyricon.lyric.model.interfaces.DeepCopyable
import io.github.proify.lyricon.lyric.model.interfaces.Normalize
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 歌曲信息
 *
 * @property id 歌曲ID
 * @property name 歌曲名
 * @property artist 艺术家
 * @property duration 歌曲时长
 * @property metadata 元数据
 * @property lyrics 歌词列表
 */
@Serializable
@Parcelize
data class Song(
    var id: String? = null,
    var name: String? = null,
    var artist: String? = null,
    var duration: Long = 0,
    var metadata: LyricMetadata? = null,
    var lyrics: List<RichLyricLine>? = null,
) : Parcelable, DeepCopyable<Song>, Normalize<Song> {

    override fun deepCopy(): Song = copy(
        lyrics = lyrics?.deepCopy()
    )

    override fun normalize(): Song = deepCopy().apply {
        lyrics = lyrics
            ?.map { line ->
                if (line.duration <= 0) {
                    line.copy(duration = line.end - line.begin)
                } else {
                    line
                }
            }
            ?.filter { line ->
                line.begin >= 0 && line.begin < line.end && line.duration > 0
            }
            ?.normalizeSortByTime()
    }
}