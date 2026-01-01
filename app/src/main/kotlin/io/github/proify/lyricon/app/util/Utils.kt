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

package io.github.proify.lyricon.app.util

import android.content.Context
import android.content.SharedPreferences
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import io.github.proify.android.extensions.getSharedPreferences
import io.github.proify.lyricon.app.Application
import java.util.Locale

object Utils {

    inline fun SharedPreferences.commitEdit(
        action: SharedPreferences.Editor.() -> Unit,
    ) {
        val editor = edit()
        action(editor)
        editor.commit()
    }

    fun getDefaultSharedPreferences(context: Context) = context.getSharedPreferences(
        context.getPackageName() + "_preferences", false
    )

    fun forceStop(packageName: String?) {
        Application.instance.packageManager.getInstalledPackages(0)
        ShellUtils.execCmd(
            "am force-stop $packageName", isRooted = true, isNeedResultMsg = true
        )
    }

    fun killSystemUi(): ShellUtils.CommandResult {
        return ShellUtils.execCmd(
            "kill -9 $(pgrep systemui)", isRooted = true, isNeedResultMsg = true
        )
    }

    fun Context.launchBrowser(
        url: String,
        toolbarColor: Int? = null,
    ) {
        val colorSchemeParamsBuilder = CustomTabColorSchemeParams.Builder()
        if (toolbarColor != null) {
            colorSchemeParamsBuilder.setToolbarColor(toolbarColor)
        }
        val customTabs = CustomTabsIntent.Builder()
            .setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM)
            .setDefaultColorSchemeParams(colorSchemeParamsBuilder.build())
            .setTranslateLocale(Locale.getDefault())
            .build()
        customTabs.launchUrl(this, url.toUri())
    }
}