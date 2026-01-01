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

package io.github.proify.android.extensions

import kotlinx.serialization.json.Json


val jsonx = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/**
 * 根据类型获取默认的 JSON 字符串
 */
inline fun <reified T> defaultJson(): String {
    val clazz = T::class.java
    return when {
        clazz.isArray -> "[]"
        Collection::class.java.isAssignableFrom(clazz) -> "[]"
        Map::class.java.isAssignableFrom(clazz) -> "{}"
        else -> "{}"
    }
}

/**
 * 安全解码 JSON，失败时返回默认值
 */
inline fun <reified T> Json.safeDecode(json: String?, default: T? = null): T {
    if (json.isNullOrBlank()) {
        return default ?: decodeFromString(defaultJson<T>())
    }

    return runCatching {
        decodeFromString<T>(json)
    }.getOrElse {
        default ?: decodeFromString(defaultJson<T>())
    }
}

/**
 * 安全编码为 JSON，失败时返回默认空结构
 */
inline fun <reified T> Json.safeEncode(value: T?): String {
    if (value == null) return defaultJson<T>()

    return runCatching {
        encodeToString(value)
    }.getOrElse {
        defaultJson<T>()
    }
}

/**
 * 扩展函数：将任意对象转换为 JSON 字符串
 */
inline fun <reified T> T.toJson(): String {
    return jsonx.safeEncode(this)
}

/**
 * 扩展函数：从 JSON 字符串解析对象
 */
inline fun <reified T> String.fromJson(default: T? = null): T {
    return jsonx.safeDecode(this, default)
}

/**
 * 扩展函数：尝试解析 JSON，返回可空结果
 */
inline fun <reified T> String.fromJsonOrNull(): T? {
    return runCatching {
        jsonx.decodeFromString<T>(this)
    }.getOrNull()
}
