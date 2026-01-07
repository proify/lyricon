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

package io.github.proify.lyricon.xposed.util

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.service.notification.StatusBarNotification
import com.highcapable.yukihookapi.hook.log.YLog
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import io.github.proify.android.extensions.saveBitmapToDisk
import io.github.proify.android.extensions.toBitmap
import io.github.proify.lyricon.xposed.Dirs
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

object NotificationCoverHelper {
    private val listeners = CopyOnWriteArrayList<OnCoverUpdateListener>()
    private const val COVER_FILE_NAME = "cover.png"

    private val NOTIFICATION_LISTENER_CLASS_CANDIDATES = arrayOf(
        "com.android.systemui.statusbar.notification.MiuiNotificationListener",
        "com.android.systemui.statusbar.NotificationListener",
    )

    fun registerListener(listener: OnCoverUpdateListener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: OnCoverUpdateListener) {
        listeners.remove(listener)
    }

    private fun findNotificationListenerClass(classLoader: ClassLoader): Class<*>? {
        for (className in NOTIFICATION_LISTENER_CLASS_CANDIDATES) {
            try {
                return classLoader.loadClass(className)
            } catch (_: ClassNotFoundException) {
                // ignored
            }
        }
        return null
    }

    fun hook(classLoader: ClassLoader) {
        val listenerClass = findNotificationListenerClass(classLoader)
        if (listenerClass == null) {
            YLog.error("未找到通知监听器类,无法 Hook 通知")
            return
        }

        try {
            XposedBridge.hookAllMethods(
                listenerClass,
                "onNotificationPosted",
                NotificationPostedHook()
            )
        } catch (e: Throwable) {
            YLog.error("Hook 通知监听器失败", e)
        }
    }

    fun getCoverFile(packageName: String): File {
        return File(Dirs.getPackageDataDir(packageName), COVER_FILE_NAME)
    }

    fun interface OnCoverUpdateListener {
        fun onCoverUpdated(packageName: String, coverFile: File)
    }

    private class NotificationPostedHook : XC_MethodHook() {

        override fun afterHookedMethod(param: MethodHookParam) {
            extractAndSaveCover(param)
        }

        private fun extractAndSaveCover(param: MethodHookParam) {
            val statusBarNotification = param.args[0] as? StatusBarNotification ?: return
            val packageName = statusBarNotification.packageName

            val notification: Notification = statusBarNotification.notification
            val largeIcon: Icon = notification.getLargeIcon() ?: return

            saveCoverIcon(largeIcon, packageName)
        }

        private fun saveCoverIcon(icon: Icon, packageName: String) {
            val context = Utils.appContext ?: run {
                YLog.warn("应用上下文为空,无法保存封面")
                return
            }

            val drawable = icon.loadDrawable(context) ?: run {
                YLog.warn("无法加载图标 Drawable")
                return
            }

            val bitmap: Bitmap = drawable.toBitmap()
            val coverFile = getCoverFile(packageName)

            bitmap.saveBitmapToDisk(coverFile.absolutePath)
            for (listener in listeners) {
                listener.onCoverUpdated(packageName, coverFile)
            }
        }
    }
}