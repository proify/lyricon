/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.hook

import android.app.Application
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import io.github.proify.lyricon.xposed.logger.YLog
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicBoolean

abstract class PackageHooker {
    companion object {
        private const val TAG = "PackageHooker"
    }

    protected lateinit var module: XposedModule
    protected lateinit var packageParam: XposedModuleInterface.PackageLoadedParam

    val packageName: String get() = packageParam.packageName
    val classLoader: ClassLoader get() = packageParam.defaultClassLoader

    @Volatile
    var appContext: Application? = null
        private set

    private val appOnCreateListeners = CopyOnWriteArraySet<(Application) -> Unit>()
    private val isHooked = AtomicBoolean(false)

    fun isMainProcess(): Boolean =
        packageParam.applicationInfo.processName == packageName

    /**
     * 在 Application 创建时执行回调。
     * 如果 Application 已创建，立即执行回调。
     * 否则，注册回调等待 Application 创建
     */
    fun doOnAppCreated(callback: (Application) -> Unit) {
        val current = appContext
        if (current != null) {
            callback(current)
            return
        }

        appOnCreateListeners.add(callback)

        if (isHooked.compareAndSet(false, true)) {
            hookApplicationOnCreate()
        }
    }

    /**
     * Hook Application.onCreate() 以获取 Application 实例
     */
    private fun hookApplicationOnCreate() {
        val targetClassName = packageParam.applicationInfo.className ?: "android.app.Application"

        try {
            YLog.info(TAG, "Targeting Application class: $targetClassName")

            val targetClass = classLoader.loadClass(targetClassName)
            val onCreateMethod = targetClass.getDeclaredMethod("onCreate")

            module.hook(onCreateMethod)
                .intercept(XposedInterfaceHooker())
        } catch (e: Throwable) {
            YLog.error(
                TAG,
                "Failed to hook $targetClassName, falling back to global Application",
                e
            )
            fallbackToGlobalHook()
        }
    }

    /**
     * 兜底方案：Hook 通用的 Application 类
     */
    private fun fallbackToGlobalHook() {
        try {
            val onCreateMethod = Application::class.java.getDeclaredMethod("onCreate")

            module.hook(onCreateMethod).intercept(
                object : XposedInterface.Hooker {
                    override fun intercept(chain: XposedInterface.Chain): Any? {
                        chain.proceed()
                        val instance = chain.thisObject as? Application
                        if (instance != null) {
                            handleApplicationInstance(instance)
                        }
                        return null
                    }
                }
            )
        } catch (t: Throwable) {
            YLog.error(TAG, "Critical failure: Global Hook failed", t)
        }
    }


    private inner class XposedInterfaceHooker : XposedInterface.Hooker {
        override fun intercept(chain: XposedInterface.Chain): Any? {
            chain.proceed()
            val instance = chain.thisObject as? Application
            if (instance != null) {
                handleApplicationInstance(instance)
            }
            return null // 或 void 不用返回
        }
    }

    private fun handleApplicationInstance(instance: Application) {
        if (appContext != null) return

        synchronized(this) {
            if (appContext == null) {
                appContext = instance

                YLog.info(TAG, "Application Context is ready: " + instance.javaClass.name)

                appOnCreateListeners.forEach {
                    runCatching { it(instance) }.onFailure {
                        YLog.error(TAG, "Callback error", it)
                    }
                }
                appOnCreateListeners.clear()
            }
        }
    }

    private var isAttached = false
    fun hook(module: XposedModule, param: XposedModuleInterface.PackageLoadedParam) {
        if (isAttached) {
            YLog.info(TAG, "Already attached")
            return
        }
        isAttached = true
        this.module = module
        this.packageParam = param
        onHook()
    }

    abstract fun onHook()
}
