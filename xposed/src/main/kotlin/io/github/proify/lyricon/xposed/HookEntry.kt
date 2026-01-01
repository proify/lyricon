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

package io.github.proify.lyricon.xposed

import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import io.github.proify.lyricon.common.PackageNames
import io.github.proify.lyricon.xposed.hook.AppHooker
import io.github.proify.lyricon.xposed.hook.systemui.SystemUIHooker
import io.github.proify.lyricon.xposed.util.Utils

@InjectYukiHookWithXposed(modulePackageName = PackageNames.APPLICATION)
open class HookEntry : IYukiHookXposedInit {

    companion object {
        private var isDebug = true
    }

    override fun onHook() = YukiHookAPI.encase {

        onAppLifecycle {
            onCreate {
                Dirs.initialize(applicationContext)
                Utils.appContext = this.applicationContext
            }
        }
        loadApp(PackageNames.APPLICATION, AppHooker)
        loadApp(PackageNames.SYSTEM_UI, SystemUIHooker)
    }

    override fun onInit() {
        super.onInit()
        YukiHookAPI.configs {
            debugLog {
                tag = "Lyricon"
                isEnable = true
                elements(TAG, PRIORITY, PACKAGE_NAME, USER_ID)
            }
        }
    }

}