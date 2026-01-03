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

import android.content.SharedPreferences
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class PackageStyle(
    var logo: LogoStyle = LogoStyle(),
    var text: TextStyle = TextStyle(),
    var anim: AnimStyle = AnimStyle()
) : AbstractStyle(), Parcelable {

    override fun onLoad(preferences: SharedPreferences) {
        logo.load(preferences)
        text.load(preferences)
        anim.load(preferences)
    }

    override fun onWrite(editor: SharedPreferences.Editor) {
        logo.write(editor)
        text.write(editor)
        anim.write(editor)
    }
}