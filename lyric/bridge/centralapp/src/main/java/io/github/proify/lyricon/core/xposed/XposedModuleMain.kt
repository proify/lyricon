/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.core.xposed

import android.app.Application
import androidx.annotation.Keep
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import io.github.proify.lyricon.central.BridgeCentral

@Keep
class XposedModuleMain : XposedModule() {

    companion object {
        private const val TAG = "Lyricon-Xposed"
        private const val TARGET_PACKAGE = "com.android.systemui"

        @Volatile
        private var isInitialized = false
    }

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        if (param.packageName != TARGET_PACKAGE) {
            return
        }

        val appClassName = param.applicationInfo.className ?: "android.app.Application"

        try {
            val appClass = Class.forName(appClassName, true, param.classLoader)
            val onCreateMethod = appClass.getDeclaredMethod("onCreate")

            hook(onCreateMethod).intercept(
                object : XposedInterface.Hooker {
                    override fun intercept(chain: XposedInterface.Chain): Any? {
                        chain.proceed()
                        val app = chain.thisObject as? Application

                        synchronized(this@XposedModuleMain) {
                            if (!isInitialized && app != null) {
                                initLyriconCentral(app)
                                isInitialized = true
                            }
                        }
                        return null
                    }
                }
            )
        } catch (e: Throwable) {
            log(
                android.util.Log.ERROR,
                TAG,
                "Failed to hook onCreate in $appClassName: " + e.message
            )
        }
    }

    private fun initLyriconCentral(app: Application) {
        try {
            BridgeCentral.initialize(app)
            BridgeCentral.sendBootCompleted()
            log(android.util.Log.INFO, TAG, "Lyricon BridgeCentral initialized successfully.")
        } catch (e: Throwable) {
            log(
                android.util.Log.ERROR,
                TAG,
                "Error during initialization: " + e.stackTraceToString()
            )
        }
    }
}
