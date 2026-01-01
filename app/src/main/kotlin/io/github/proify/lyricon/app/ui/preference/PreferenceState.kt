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

package io.github.proify.lyricon.app.ui.preference

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import io.github.proify.lyricon.app.util.Utils.commitEdit

/**
 * 通用 PreferenceState,支持动态 SharedPreferences 实例
 */
@Composable
fun <T> rememberPreference(
    sharedPreferences: SharedPreferences,
    key: String,
    defaultValue: T,
    getter: SharedPreferences.(String, T) -> T,
    setter: SharedPreferences.Editor.(String, T) -> SharedPreferences.Editor
): MutableState<T> {
    // 确保闭包中使用最新 sharedPreferences
    val currentPrefs by rememberUpdatedState(sharedPreferences)

    // 添加 sharedPreferences 作为 remember 的 key
    val state = remember(sharedPreferences, key) {
        mutableStateOf(currentPrefs.getter(key, defaultValue))
    }

    // 当 sharedPreferences 对象变化时,立即更新值
    LaunchedEffect(sharedPreferences, key) {
        val newValue = sharedPreferences.getter(key, defaultValue)
        if (state.value != newValue) {
            state.value = newValue
        }
    }

    DisposableEffect(currentPrefs, key) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sp, changedKey ->
            if (changedKey == key) {
                val newValue = sp.getter(key, defaultValue)
                if (state.value != newValue) state.value = newValue
            }
        }
        currentPrefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { currentPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    // 将包装对象也用 remember 包裹,避免每次重组时重新创建
    return remember(sharedPreferences, key) {
        object : MutableState<T> {
            override var value: T
                get() = state.value
                set(newValue) {
                    currentPrefs.commitEdit {
                        setter(key, newValue)
                    }
                    state.value = newValue
                }

            override fun component1() = value
            override fun component2() = { v: T -> value = v }
        }
    }
}

/** Boolean 简化版 */
@Composable
fun rememberBooleanPreference(
    sharedPreferences: SharedPreferences,
    key: String,
    defaultValue: Boolean = false
) =
    rememberPreference(
        sharedPreferences,
        key,
        defaultValue,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )

/** String 简化版 */
@Composable
fun rememberStringPreference(
    sharedPreferences: SharedPreferences,
    key: String,
    defaultValue: String? = null
) =
    rememberPreference(
        sharedPreferences,
        key,
        defaultValue,
        SharedPreferences::getString,
        SharedPreferences.Editor::putString
    )

/** Int 简化版 */
@Composable
fun rememberIntPreference(
    sharedPreferences: SharedPreferences,
    key: String,
    defaultValue: Int = 0
) =
    rememberPreference(
        sharedPreferences,
        key,
        defaultValue,
        SharedPreferences::getInt,
        SharedPreferences.Editor::putInt
    )

/** Long 简化版 */
@Composable
fun rememberLongPreference(
    sharedPreferences: SharedPreferences,
    key: String,
    defaultValue: Long = 0L
) =
    rememberPreference(
        sharedPreferences,
        key,
        defaultValue,
        SharedPreferences::getLong,
        SharedPreferences.Editor::putLong
    )

/** Float 简化版 */
@Composable
fun rememberFloatPreference(
    sharedPreferences: SharedPreferences,
    key: String,
    defaultValue: Float = 0f
) =
    rememberPreference(
        sharedPreferences,
        key,
        defaultValue,
        SharedPreferences::getFloat,
        SharedPreferences.Editor::putFloat
    )