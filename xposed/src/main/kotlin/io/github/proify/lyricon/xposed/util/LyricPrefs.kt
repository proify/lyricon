package io.github.proify.lyricon.xposed.util

import io.github.proify.lyricon.app.bridge.Bridge
import io.github.proify.lyricon.common.util.JsonSharedPreferences
import io.github.proify.lyricon.lyric.style.BasicStyle
import io.github.proify.lyricon.lyric.style.LyricStyle
import io.github.proify.lyricon.lyric.style.PackageStyle
import io.github.proify.lyricon.xposed.Dirs

object LyricPrefs {

    private val prefsCache = mutableMapOf<String, JsonSharedPreferences>()
    private val packageStyleCache = mutableMapOf<String, PackageStyleCache>()

    var activePackageName: String? = null

    /* ---------------- base style ---------------- */

    private val baseStylePrefs: JsonSharedPreferences =
        createXPrefs(Bridge.LyricStylePrefs.PREF_NAME_BASE_STYLE)

    val baseStyle: BasicStyle = BasicStyle().apply {
        load(baseStylePrefs)
    }
        get() {
            if (baseStylePrefs.hasFileChanged()) {
                field.load(baseStylePrefs)
            }
            return field
        }

    /* ---------------- default package style ---------------- */

    private val defaultPackageStylePrefs: JsonSharedPreferences by lazy {
        getPackagePrefs(
            Bridge.LyricStylePrefs.DEFAULT_PACKAGE_NAME
        )
    }

    val defaultPackageStyle: PackageStyle = PackageStyle().apply {
        load(defaultPackageStylePrefs)
    }
        get() {
            if (defaultPackageStylePrefs.hasFileChanged()) {
                field.load(defaultPackageStylePrefs)
            }
            return field
        }

    /* ---------------- package manager ---------------- */

    private val packageStyleManagerPrefs: JsonSharedPreferences =
        createXPrefs(Bridge.LyricStylePrefs.PREF_PACKAGE_STYLE_MANAGER)

    fun getActivePackageStyle(): PackageStyle {
        val pkg = activePackageName
        return if (pkg != null && isPackageEnabled(pkg)) {
            getPackageStyle(pkg)
        } else {
            defaultPackageStyle
        }
    }

    private fun isPackageEnabled(packageName: String): Boolean {
        return runCatching {
            packageStyleManagerPrefs
                .getStringSet(
                    Bridge.LyricStylePrefs.KEY_ENABLED_PACKAGES,
                    emptySet()
                )
                .contains(packageName)
        }.getOrDefault(false)
    }

    /* ---------------- prefs cache ---------------- */


    private fun getPackagePrefName(packageName: String): String =
        Bridge.LyricStylePrefs.getPackageStylePreferenceName(packageName)

    private fun getPackagePrefs(packageName: String): JsonSharedPreferences {
        val prefName = getPackagePrefName(packageName)
        return prefsCache.getOrPut(prefName) {
            createXPrefs(prefName)
        }
    }

    private fun createXPrefs(name: String): JsonSharedPreferences {
        val file = Dirs.getPreferenceFile(name)
        return JsonSharedPreferences(file)
    }

    /* ---------------- package style cache ---------------- */

    private class PackageStyleCache(
        private val prefs: JsonSharedPreferences,
        private val style: PackageStyle
    ) {
        fun getStyle(): PackageStyle {
            if (prefs.hasFileChanged()) {
                style.load(prefs)
            }
            return style
        }
    }

    fun getPackageStyle(packageName: String): PackageStyle {
        return packageStyleCache.getOrPut(packageName) {
            val prefs = getPackagePrefs(packageName)
            val style = PackageStyle().apply {
                load(prefs)
            }
            PackageStyleCache(prefs, style)
        }.getStyle()
    }

    /* ---------------- lyric style ---------------- */

    fun getLyricStyle(packageName: String? = null): LyricStyle {
        if (packageName == null) {
            return LyricStyle(baseStyle, getActivePackageStyle())
        }
        return LyricStyle(
            baseStyle,
            getPackageStyle(packageName)
        )
    }

}