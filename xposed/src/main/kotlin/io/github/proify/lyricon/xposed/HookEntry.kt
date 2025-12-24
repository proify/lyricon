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