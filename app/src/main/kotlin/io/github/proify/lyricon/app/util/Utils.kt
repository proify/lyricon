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