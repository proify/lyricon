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

package io.github.proify.lyricon.app.ui.activity.lyric

import android.content.SharedPreferences
import com.highcapable.yukihookapi.hook.factory.dataChannel
import io.github.proify.lyricon.app.Application
import io.github.proify.lyricon.app.bridge.BridgeConstants
import io.github.proify.lyricon.app.ui.activity.BaseActivity
import io.github.proify.lyricon.common.PackageNames

abstract class BaseLyricActivity : BaseActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    val systemUIChannel by lazy {
        dataChannel(packageName = PackageNames.SYSTEM_UI)
    }

    fun updateLyricStyle() {
        Application.MAIN_HANDLER.postDelayed({
            systemUIChannel.put(BridgeConstants.REQUEST_UPDATE_LYRIC_STYLE)
        }, 0)
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?,
        key: String?
    ) {
        if (key?.startsWith("lyric_style_") == true) {
            updateLyricStyle()
        }
    }
}