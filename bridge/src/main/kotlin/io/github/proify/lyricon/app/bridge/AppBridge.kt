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

@file:Suppress("unused")

package io.github.proify.lyricon.app.bridge

import android.content.Context
import androidx.annotation.Keep
import com.highcapable.yukihookapi.YukiHookAPI
import io.github.proify.lyricon.common.Constants
import java.io.File

object AppBridge {

    @Keep
    fun isModuleActive(): Boolean =
        runCatching {
            YukiHookAPI.Status.isXposedModuleActive
        }.getOrDefault(false)

    @Keep
    fun getPreferenceDirectory(context: Context): File = File(context.filesDir, "preferences")

    object LyricStylePrefs {
        const val DEFAULT_PACKAGE_NAME: String = Constants.APP_PACKAGE_NAME
        const val PREF_NAME_BASE_STYLE: String = "baseLyricStyle"
        const val PREF_PACKAGE_STYLE_MANAGER: String = "packageStyleManager"
        const val KEY_ENABLED_PACKAGES: String = "enables"

        fun getPackageStylePreferenceName(packageName: String): String =
            "package_style_${packageName.replace(".", "_")}"

    }
}