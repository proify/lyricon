/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.app.util

import android.content.SharedPreferences
import io.github.proify.android.extensions.getPrivateSharedPreferences
import io.github.proify.android.extensions.json
import io.github.proify.android.extensions.safeDecode
import io.github.proify.android.extensions.toJson
import io.github.proify.lyricon.app.LyriconApp
import io.github.proify.lyricon.app.bridge.AppBridge.LyricStylePrefs
import io.github.proify.lyricon.app.bridge.AppBridge.LyricStylePrefs.KEY_CONFIGURED_PACKAGES
import io.github.proify.lyricon.app.bridge.AppBridge.LyricStylePrefs.KEY_ENABLED_PACKAGES
import io.github.proify.lyricon.lyric.style.BasicStyle
import io.github.proify.lyricon.lyric.style.VisibilityRule

object LyricPrefs {

    const val DEFAULT_PACKAGE_NAME: String = LyricStylePrefs.DEFAULT_PACKAGE_NAME

    private val packageStyleManager: SharedPreferences =
        getSharedPreferences(LyricStylePrefs.PREF_NAME_PACKAGE_MANAGER)

    val basicStylePrefs: SharedPreferences
        get() = getSharedPreferences(LyricStylePrefs.PREF_NAME_BASE)

    fun getSharedPreferences(name: String): SharedPreferences {
        val xposedService = LyriconApp.xposedService
        if (xposedService != null) {
            return xposedService.getRemotePreferences(name)
        }

        return LyriconApp.get().getPrivateSharedPreferences(name)
    }

    fun isLyricStylePrefName(name: String): Boolean {
        return name.startsWith(LyricStylePrefs.LYRIC_STYLE_PREF_NAME_PREIFY)
    }

    fun getLyricStylePrefNames() = mutableListOf<String>().apply {
        add(LyricStylePrefs.PREF_NAME_BASE)
        add(LyricStylePrefs.PREF_NAME_PACKAGE_MANAGER)

        getConfiguredPackageNames().forEach {
            add(getPackagePrefName(it))
        }
    }

    fun getPackagePrefName(packageName: String): String =
        LyricStylePrefs.getPackageStylePrefName(packageName)

    fun setEnabledPackageNames(names: Set<String>) {
        packageStyleManager.editCommit {
            putStringSet(KEY_ENABLED_PACKAGES, names)
        }
    }

    fun getEnabledPackageNames(): Set<String> {
        return packageStyleManager
            .getStringSet(KEY_ENABLED_PACKAGES, null)?.toSet() ?: emptySet()
    }

    fun setConfiguredPackageNames(names: Set<String>) {
        packageStyleManager.editCommit {
            putString(KEY_CONFIGURED_PACKAGES, names.toJson())
        }
    }

    fun getConfiguredPackageNames(): Set<String> {
        val jsonData = packageStyleManager.getString(KEY_CONFIGURED_PACKAGES, null)
        return json.safeDecode<List<String>>(jsonData).toSet()
    }

    fun setViewVisibilityRule(rules: List<VisibilityRule>?) {
        basicStylePrefs.editCommit {
            if (rules.isNullOrEmpty()) {
                remove(BasicStyle.PREF_KEY_VISIBILITY_RULES)
            } else {
                putString(BasicStyle.PREF_KEY_VISIBILITY_RULES, rules.toJson())
            }
        }
    }

    fun getViewVisibilityRule(): List<VisibilityRule> {
        val configuredRules = json.safeDecode<List<VisibilityRule>>(
            basicStylePrefs.getString(BasicStyle.PREF_KEY_VISIBILITY_RULES, null),
            emptyList()
        )
        return BasicStyle.resolveVisibilityRules(configuredRules)
    }
}
