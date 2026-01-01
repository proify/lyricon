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