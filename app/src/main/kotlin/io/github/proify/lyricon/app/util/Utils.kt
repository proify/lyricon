package io.github.proify.lyricon.app.util

import android.content.Context
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import io.github.proify.lyricon.app.Application

object Utils {

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

    fun launchBrowser(
        context: Context,
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
            .build()
        customTabs.launchUrl(context, url.toUri())
    }
}