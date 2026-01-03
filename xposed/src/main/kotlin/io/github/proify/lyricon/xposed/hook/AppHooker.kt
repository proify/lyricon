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

package io.github.proify.lyricon.xposed.hook

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import io.github.proify.lyricon.app.bridge.AppBridge
import io.github.proify.lyricon.xposed.Dirs

object AppHooker : YukiBaseHooker() {

    override fun onHook() {
        replaceGetPreferenceDirectory()
    }

    private fun replaceGetPreferenceDirectory() {
        val preferenceDirectory = Dirs.preferenceDirectory ?: return
        AppBridge::class.java.name.toClass(appClassLoader)
            .resolve().apply {
                firstMethod {
                    name = "getPreferenceDirectory"
                }.hook {
                    replaceTo(preferenceDirectory)
                }
            }
    }
}