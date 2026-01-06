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
package io.github.proify.lyricon.xposed.util

import android.content.Context
import android.content.res.Resources
import android.view.View
import io.github.proify.lyricon.xposed.util.ResourceMapper.NO_ID
import java.util.concurrent.ConcurrentHashMap

/**
 * 资源 ID 与名称的双向映射工具类
 *
 * 提供资源 ID 和名称之间的缓存映射,避免重复的资源查询操作
 */
object ResourceMapper {
    const val NO_ID = View.NO_ID

    // ID -> 名称映射缓存
    private val nameCache = ConcurrentHashMap<Int, String?>()

    // 名称 -> ID映射缓存
    private val idCache = ConcurrentHashMap<String, Int>()

    /**
     * 根据资源名称获取资源 ID
     *
     * @param context 上下文对象
     * @param name 资源名称
     * @return 资源 ID,如果未找到则返回 [NO_ID]
     */
    fun getIdByName(context: Context, name: String): Int {
        // 先从缓存中查找
        idCache[name]?.let { return it }

        // 缓存未命中,执行查询
        val id = runCatching {
            context.resources.getIdentifier(name, "id", context.packageName)
        }.getOrDefault(NO_ID)

        // 更新缓存
        if (id != NO_ID) {
            idCache[name] = id
            nameCache[id] = name
        } else {
            // 即使查询失败也缓存结果,避免重复查询
            idCache[name] = NO_ID
        }

        return id
    }

    /**
     * 获取 View 的资源名称
     *
     * @param view 目标 View
     * @param resources 资源对象,默认使用 View 的 resources
     * @return 资源名称,如果 View 没有 ID 或查询失败则返回 null
     */
    fun getIdName(
        view: View,
        resources: Resources = view.resources
    ): String? {
        val id = view.id
        if (id == NO_ID) return null

        // 先从缓存中查找
        nameCache[id]?.let { return it }

        // 缓存未命中,执行查询
        val name = runCatching {
            resources.getResourceEntryName(id)
        }.getOrNull()

        // 更新缓存
        nameCache[id] = name
        name?.let { idCache[it] = id }

        return name
    }

    /**
     * 清除所有缓存
     *
     * 在资源变化(如主题切换)时可能需要调用此方法
     */
    fun clearCache() {
        nameCache.clear()
        idCache.clear()
    }

    /**
     * 获取缓存统计信息
     *
     * @return Pair<名称缓存大小, ID缓存大小>
     */
    fun getCacheStats(): Pair<Int, Int> {
        return nameCache.size to idCache.size
    }
}