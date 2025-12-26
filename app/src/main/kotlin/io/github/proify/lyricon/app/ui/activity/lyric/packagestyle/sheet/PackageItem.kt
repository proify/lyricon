package io.github.proify.lyricon.app.ui.activity.lyric.packagestyle.sheet

import android.content.pm.ApplicationInfo
import io.github.proify.lyricon.app.Application
import io.github.proify.lyricon.app.util.LyricPrefs.DEFAULT_PACKAGE_NAME

data class PackageItem(
    val applicationInfo: ApplicationInfo,
) {
    val isDefault: Boolean
        get() = applicationInfo.packageName == DEFAULT_PACKAGE_NAME

    fun getLabel(): String {
        val cached = AppCache.getCachedLabel(applicationInfo.packageName)
        if (cached != null) return cached

        val context = Application.instance
        val packageManager = context.packageManager
        val label = applicationInfo.loadLabel(packageManager).toString()
        AppCache.cacheLabel(applicationInfo.packageName, label)
        return label
    }

}