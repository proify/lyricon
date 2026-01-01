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

package io.github.proify.lyricon.xposed.util

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.widget.TextView
import com.highcapable.yukihookapi.hook.log.YLog
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import io.github.proify.android.extensions.isDarkAgainst
import java.lang.reflect.Member
import java.util.concurrent.CopyOnWriteArraySet

object StatusBarColorMonitor {
    private val colorCache = mutableMapOf<Int, Boolean>()

    private val colorChangeListeners = CopyOnWriteArraySet<OnColorChangeListener>()
    private val hookUnlocks = CopyOnWriteArraySet<XC_MethodHook.Unhook>()

    fun register(listener: OnColorChangeListener) {
        colorChangeListeners.add(listener)
    }

    fun unregister(listener: OnColorChangeListener) {
        colorChangeListeners.remove(listener)
    }

    fun hook(targetClass: Class<out View>) {
        unhookAll()
        val classLoader = targetClass.classLoader
        val methods = findMethodsRecursively(targetClass, "onDarkChanged")

        methods.forEach { method ->
            hookUnlocks.add(
                XposedBridge.hookMethod(method, DarkChangedMethodHook(classLoader))
            )
        }
    }

    @Suppress("SameParameterValue")
    private fun findMethodsRecursively(
        clazz: Class<*>?,
        methodName: String
    ): List<Member> {
        val result = mutableListOf<Member>()
        var currentClass = clazz

        while (currentClass != null) {
            currentClass.declaredMethods
                .filter { it.name == methodName }
                .forEach { method ->
                    method.isAccessible = true
                    result.add(method)
                    return result
                }
            currentClass = currentClass.superclass
        }
        return result
    }

    fun unhookAll() {
        hookUnlocks.forEach { it.unhook() }
        hookUnlocks.clear()
    }

    fun interface OnColorChangeListener {
        fun onColorChange(color: StatusColor)
    }

    private class DarkChangedMethodHook(
        private val classLoader: ClassLoader?
    ) : XC_MethodHook() {

        private var hasNonAdaptedColorField = true
        private var canCallTintMethod = true

        override fun afterHookedMethod(param: MethodHookParam) {
            val any = param.thisObject
            val color = getStatusColor(param, any)
            if (color == 0) return

            colorChangeListeners.forEach {
                runCatching {
                    val isLight = colorCache.getOrPut(color) {
                        color.isDarkAgainst(Color.BLACK)
                    }
                    it.onColorChange(StatusColor(color, isLight))
                }.onFailure { it -> YLog.error(it) }
            }
        }

        private fun getStatusColor(param: MethodHookParam, any: Any): Int {
            // 尝试从反射字段获取
            tryGetNonAdaptedColor(any)?.let { return it }

            // 尝试从 DarkIconDispatcher 获取
            tryGetTintColor(param)?.let { return it }

            // 降级到当前文本颜色
            return if (any is TextView) any.currentTextColor else 0
        }

        @SuppressLint("PrivateApi")
        private fun tryGetNonAdaptedColor(any: Any): Int? {
            if (!hasNonAdaptedColorField) return null

            return runCatching {
                XposedHelpers.getIntField(any, "mNonAdaptedColor")
            }.onFailure {
                hasNonAdaptedColorField = false
                YLog.error(it)
            }.getOrNull()
        }

        private fun tryGetTintColor(param: MethodHookParam): Int? {
            if (!canCallTintMethod || param.args.size != 3) return null

            return runCatching {
                val dispatcher = darkIconDispatcher ?: classLoader
                    ?.loadClass("com.android.systemui.plugins.DarkIconDispatcher")
                    ?.also { darkIconDispatcher = it }

                XposedHelpers.callStaticMethod(
                    dispatcher,
                    "getTint",
                    arrayOf(
                        Collection::class.java,
                        View::class.java,
                        Int::class.javaPrimitiveType
                    ),
                    param.args[0],
                    param.thisObject,
                    param.args[2]
                ) as Int
            }.onFailure {
                canCallTintMethod = false
                YLog.error(it)
            }.getOrNull()
        }

        companion object {
            private var darkIconDispatcher: Class<*>? = null
        }
    }
}