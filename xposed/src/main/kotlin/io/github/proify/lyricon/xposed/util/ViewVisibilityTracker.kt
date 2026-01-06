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
package io.github.proify.lyricon.xposed.util

import android.view.View
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.proify.lyricon.xposed.hook.AppHooker.hook
import java.util.concurrent.ConcurrentHashMap

/**
 * 视图可见性追踪器
 * 通过 Hook View.setFlags 方法来追踪和管理视图的原始可见性状态
 */
object ViewVisibilityTracker {

    // 用于标记被追踪的视图
    const val TRACKING_TAG_ID = 0x7F137666

    // 自定义可见性标记(避免与系统常量冲突)
    const val CUSTOM_VISIBLE = 123456789
    const val CUSTOM_GONE = 987654321

    // Android 系统可见性标志掩码
    private const val VISIBILITY_FLAG_MASK = 0x0000000C

    // 存储视图原始可见性状态
    private val originalVisibilityMap = ConcurrentHashMap<Int, Int>()

    private var hookResult: YukiMemberHookCreator.MemberHookCreator.Result? = null

    /**
     * 初始化 Hook
     */
    fun initialize(classLoader: ClassLoader) {
        hookResult = View::class.resolve()
            .firstMethod {
                name = "setFlags"
                parameters(Int::class.java, Int::class.java)
            }.hook {
                before {
                    handleSetFlags(instance as View, args)
                }
            }
        YLog.debug("ViewVisibilityTracker initialized. hookResult: " + hookResult)
    }

    private fun handleSetFlags(view: View, args: Array<Any?>) {
        val viewId = view.id
        if (viewId == View.NO_ID) return

        val flags = args[0] as Int
        val mask = args[1] as Int

        // 只处理可见性相关的标志
        if (mask != VISIBILITY_FLAG_MASK) return

        when (flags) {
            CUSTOM_GONE -> {
                saveOriginalVisibilityIfNeeded(viewId, view.visibility)
                args[0] = View.GONE
            }

            CUSTOM_VISIBLE -> {
                saveOriginalVisibilityIfNeeded(viewId, view.visibility)
                args[0] = View.VISIBLE
            }

            else -> {
                // 只有当视图被标记为追踪时,才保存系统改变的可见性
                if (view.getTag(TRACKING_TAG_ID) != null) {
                    originalVisibilityMap[viewId] = flags
                }
            }
        }
    }

    private fun saveOriginalVisibilityIfNeeded(viewId: Int, currentVisibility: Int) {
        if (!originalVisibilityMap.containsKey(viewId)) {
            originalVisibilityMap[viewId] = currentVisibility
        }
    }

    /**
     * 获取视图的原始可见性
     * @return 原始可见性值,如果不存在则返回默认值
     */
    fun getOriginalVisibility(viewId: Int, defaultValue: Int = -1): Int {
        return originalVisibilityMap.getOrDefault(viewId, defaultValue)
    }

    /**
     * 清除指定视图的追踪记录
     */
    fun clearTracking(viewId: Int) {
        originalVisibilityMap.remove(viewId)
    }

    /**
     * 清除所有追踪记录
     */
    fun clearAllTracking() {
        originalVisibilityMap.clear()
    }
}