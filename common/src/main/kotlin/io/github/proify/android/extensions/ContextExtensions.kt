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

package io.github.proify.android.extensions

import android.content.Context
import android.content.SharedPreferences

fun Context.getPrivateSharedPreferences(name: String): SharedPreferences =
    getSharedPreferences(name, Context.MODE_PRIVATE)

/**
 * 尝试获取 world-readable 的 SharedPreferences，失败则返回私有的
 */
fun Context.getWorldReadableSharedPreferences(name: String): SharedPreferences = try {
    @Suppress("DEPRECATION") getSharedPreferences(name, Context.MODE_WORLD_READABLE)
} catch (_: Exception) {
    getPrivateSharedPreferences(name)
}

fun Context.getSharedPreferences(name: String, worldReadable: Boolean): SharedPreferences =
    if (worldReadable) getWorldReadableSharedPreferences(name)
    else getPrivateSharedPreferences(name)