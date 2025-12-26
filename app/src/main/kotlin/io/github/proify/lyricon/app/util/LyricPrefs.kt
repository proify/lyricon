package io.github.proify.lyricon.app.util

import android.content.SharedPreferences
import io.github.proify.android.extensions.fromJson
import io.github.proify.android.extensions.getSharedPreferences
import io.github.proify.android.extensions.jsonx
import io.github.proify.android.extensions.safeDecode
import io.github.proify.android.extensions.toJson
import io.github.proify.lyricon.app.Application
import io.github.proify.lyricon.app.bridge.Bridge.LyricStylePrefs
import io.github.proify.lyricon.app.bridge.Bridge.LyricStylePrefs.KEY_ENABLED_PACKAGES
import io.github.proify.lyricon.app.util.Utils.commitEdit
import io.github.proify.lyricon.lyric.style.VisibilityRule

object LyricPrefs {

    const val DEFAULT_PACKAGE_NAME = LyricStylePrefs.DEFAULT_PACKAGE_NAME
    const val KEY_CONFIGURED_PACKAGES = "configured"

    private val packageStyleManager: SharedPreferences =
        getSharedPreferences(LyricStylePrefs.PREF_PACKAGE_STYLE_MANAGER)

    val basicStylePrefs: SharedPreferences
        get() = getSharedPreferences(LyricStylePrefs.PREF_NAME_BASE_STYLE)

    fun getSharedPreferences(name: String): SharedPreferences {
        return Application.instance.getSharedPreferences(name, worldReadable = true)
    }

    fun getPackagePrefName(packageName: String) =
        LyricStylePrefs.getPackageStylePreferenceName(packageName)

    fun setEnabledPackageNames(names: Set<String>) {
        packageStyleManager.commitEdit {
            putStringSet(KEY_ENABLED_PACKAGES, names)
        }
    }

    fun getEnabledPackageNames(): Set<String> {
        return packageStyleManager
            .getStringSet(KEY_ENABLED_PACKAGES, null)?.toSet() ?: emptySet()
    }

    fun setConfiguredPackageNames(names: Set<String>) {
        packageStyleManager.commitEdit {
            putString(KEY_CONFIGURED_PACKAGES, names.toJson())
        }
    }

    fun getConfiguredPackageNames(): Set<String> {
        val json = packageStyleManager.getString(KEY_CONFIGURED_PACKAGES, null)
        return jsonx.safeDecode<MutableList<String>>(json).toSet()
    }

    fun setViewVisibilityRule(rules: List<VisibilityRule>) {
        basicStylePrefs.commitEdit {
            putString("lyric_style_base_visibility_rules", rules.toJson())
        }
    }

    fun getViewVisibilityRule(): List<VisibilityRule> {
        val json = basicStylePrefs.getString("lyric_style_base_visibility_rules", null)
        return json?.fromJson<List<VisibilityRule>>() ?: emptyList()
    }

}