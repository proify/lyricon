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

package io.github.proify.lyricon.lyric.style

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class VisibilityRule(
    val id: String,
    var mode: Int
) : Parcelable {
    companion object {
        const val MODE_NORMAL: Int = 0
        const val MODE_ALWAYS_VISIBLE: Int = 1
        const val MODE_ALWAYS_HIDDEN: Int = 2
        const val MODE_HIDE_WHEN_PLAYING: Int = 3
    }
}