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

package io.github.proify.lyricon.common.util

import android.content.SharedPreferences
import android.util.Log

/**
 * SharedPreferences的安全包装类,提供类型转换容错能力
 * 当读取的值类型不匹配时,会尝试类型转换,转换失败则返回默认值
 */
class SafePreferences private constructor(
    val delegate: SharedPreferences
) : SharedPreferences by delegate {

    /**
     * 安全读取值的通用方法
     * @param primary 主要读取方法
     * @param fallback 类型转换失败时的备用方法
     * @param default 默认值
     * @param key 键名(用于日志)
     */
    private inline fun <T> safeRead(
        key: String,
        primary: () -> T,
        fallback: () -> T,
        default: T
    ): T = runCatching {
        primary()
    }.recoverCatching { primaryError ->
        if (DEBUG) {
            Log.w(TAG, "Primary read failed for key '$key': ${primaryError.message}")
        }
        fallback()
    }.getOrElse { fallbackError ->
        if (DEBUG) {
            Log.w(
                TAG,
                "Fallback read failed for key '$key': ${fallbackError.message}, using default"
            )
        }
        default
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean = safeRead(
        key = key,
        primary = { delegate.getBoolean(key, defValue) },
        fallback = {
            val value = delegate.all[key]
            when (value) {
                is Boolean -> value
                is String -> value.toBooleanStrictOrNull()
                is Number -> value.toInt() != 0
                else -> null
            } ?: defValue
        },
        default = defValue
    )

    override fun getFloat(key: String, defValue: Float): Float = safeRead(
        key = key,
        primary = { delegate.getFloat(key, defValue) },
        fallback = {
            delegate.all[key]?.let { value ->
                when (value) {
                    is Float -> value
                    is Number -> value.toFloat()
                    is String -> value.toFloatOrNull()
                    else -> null
                }
            } ?: defValue
        },
        default = defValue
    )

    override fun getInt(key: String, defValue: Int): Int = safeRead(
        key = key,
        primary = { delegate.getInt(key, defValue) },
        fallback = {
            delegate.all[key]?.let { value ->
                when (value) {
                    is Int -> value
                    is Number -> value.toInt()
                    is String -> value.toIntOrNull()
                    else -> null
                }
            } ?: defValue
        },
        default = defValue
    )

    override fun getLong(key: String, defValue: Long): Long = safeRead(
        key = key,
        primary = { delegate.getLong(key, defValue) },
        fallback = {
            delegate.all[key]?.let { value ->
                when (value) {
                    is Long -> value
                    is Number -> value.toLong()
                    is String -> value.toLongOrNull()
                    else -> null
                }
            } ?: defValue
        },
        default = defValue
    )

    override fun getString(key: String, defValue: String?): String? = safeRead(
        key = key,
        primary = { delegate.getString(key, defValue) },
        fallback = { delegate.all[key]?.toString() ?: defValue },
        default = defValue
    )

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? = safeRead(
        key = key,
        primary = { delegate.getStringSet(key, defValues)?.toSet() }, // 返回不可变副本
        fallback = {
            @Suppress("UNCHECKED_CAST")
            (delegate.all[key] as? Set<String>)?.toSet() ?: defValues
        },
        default = defValues
    )

    override fun edit(): SharedPreferences.Editor = SafeEditor(delegate.edit())

    /**
     * 安全的Editor包装类
     */
    private class SafeEditor(
        private val delegate: SharedPreferences.Editor
    ) : SharedPreferences.Editor by delegate {

        override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor {
            // 创建不可变副本,防止外部修改
            delegate.putStringSet(key, values?.toSet())
            return this
        }
    }

    companion object {
        private const val TAG = "SafePreferences"
        private const val DEBUG = false // 生产环境设为false

        /**
         * 将普通SharedPreferences转换为SafePreferences
         * 如果已经是SafePreferences则直接返回,避免重复包装
         */
        fun from(preferences: SharedPreferences): SafePreferences =
            if (preferences is SafePreferences) {
                preferences
            } else {
                SafePreferences(preferences)
            }
    }
}

/**
 * SharedPreferences扩展函数,快速转换为SafePreferences
 */
fun SharedPreferences.safe(): SafePreferences = SafePreferences.from(this)

/**
 * SafePreferences扩展函数,获取原始delegate
 */
fun SafePreferences.raw(): SharedPreferences = this.delegate