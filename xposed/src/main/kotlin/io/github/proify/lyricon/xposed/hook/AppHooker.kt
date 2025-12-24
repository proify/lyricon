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