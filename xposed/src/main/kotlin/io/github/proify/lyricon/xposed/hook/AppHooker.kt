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

package io.github.proify.lyricon.xposed.hook

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import io.github.proify.lyricon.app.bridge.Bridge
import io.github.proify.lyricon.xposed.Dirs

object AppHooker : YukiBaseHooker() {

    override fun onHook() {
        Bridge::class.java.name.toClass(appClassLoader)
            .resolve().apply {
                firstMethod {
                    name = "getPreferenceDirectory"
                }.hook {
                    replaceTo(Dirs.preferenceDirectory)
                }
                firstMethod {
                    name = "getPreferenceFile"
                }.hook {
                    after {
                        val name = args(1).string()
                        if (name.isBlank().not()) {
                            result = Dirs.getPreferenceFile(name)
                        }
                    }
                }
            }
    }

}