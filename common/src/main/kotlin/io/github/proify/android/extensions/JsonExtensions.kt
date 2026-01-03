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

import kotlinx.serialization.json.Json


val jsonx = Json {
    ignoreUnknownKeys = true
    encodeDefaults = false
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
 * 将任意对象转换为 JSON 字符串
 */
inline fun <reified T> T.toJson(): String {
    return jsonx.safeEncode(this)
}

/**
 * 从 JSON 字符串解析对象
 */
inline fun <reified T> String.fromJson(default: T? = null): T {
    return jsonx.safeDecode(this, default)
}

/**
 * 尝试解析 JSON，返回可空结果
 */
inline fun <reified T> String.fromJsonOrNull(): T? {
    return runCatching {
        jsonx.decodeFromString<T>(this)
    }.getOrNull()
}
