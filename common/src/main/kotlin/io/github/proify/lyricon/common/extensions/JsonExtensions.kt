package io.github.proify.lyricon.common.extensions

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
