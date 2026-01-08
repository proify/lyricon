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

package io.github.proify.lyricon.app.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Process
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.edit
import androidx.core.net.toUri
import io.github.proify.android.extensions.getWorldReadableSharedPreferences
import io.github.proify.lyricon.app.ui.activity.MainActivity
import java.util.Locale

object Utils {
    inline fun SharedPreferences.commitEdit(action: SharedPreferences.Editor.() -> Unit): Unit =
        edit(commit = true) { action() }

    fun getDefaultSharedPreferences(context: Context): SharedPreferences =
        context.getWorldReadableSharedPreferences(context.packageName + "_preferences")

    fun Activity.restartApp() {
        val intent =
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        startActivity(intent)
        finish()

        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
        Process.killProcess(Process.myPid())
    }

    fun forceStop(packageName: String?): ShellUtils.CommandResult =
        ShellUtils.execCmd(
            "am force-stop $packageName",
            isRoot = true,
            isNeedResultMsg = true,
        )

    fun killSystemUI(): ShellUtils.CommandResult =
        ShellUtils.execCmd(
            "kill -9 $(pgrep systemui)",
            isRoot = true,
            isNeedResultMsg = true,
        )

    fun Context.launchBrowser(
        url: String,
        toolbarColor: Int? = null,
    ) {
        val colorSchemeParamsBuilder = CustomTabColorSchemeParams.Builder()
        if (toolbarColor != null) {
            colorSchemeParamsBuilder.setToolbarColor(toolbarColor)
        }
        val customTabs =
            CustomTabsIntent
                .Builder()
                .setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM)
                .setDefaultColorSchemeParams(colorSchemeParamsBuilder.build())
                .setTranslateLocale(Locale.getDefault())
                .build()
        customTabs.launchUrl(this, url.toUri())
    }
}