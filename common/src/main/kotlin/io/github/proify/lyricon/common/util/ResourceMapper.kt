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

import android.content.Context
import android.content.res.Resources
import android.view.View
import java.util.concurrent.ConcurrentHashMap

object ResourceMapper {
    const val NO_ID: Int = View.NO_ID

    private val nameCache = ConcurrentHashMap<Int, String>()
    private val idCache = ConcurrentHashMap<String, Int>()

    fun getIdByName(
        context: Context,
        name: String,
    ): Int {
        idCache[name]?.let { return it }

        val id =
            runCatching {
                context.resources.getIdentifier(name, "id", context.packageName)
            }.getOrDefault(NO_ID)

        if (id != NO_ID) {
            idCache[name] = id
            nameCache[id] = name
        } else {
            idCache[name] = NO_ID
        }

        return id
    }

    fun getIdName(
        view: View,
        resources: Resources = view.resources,
    ): String? = getIdName(view.id, resources)

    fun getIdName(
        id: Int,
        resources: Resources,
    ): String? {
        if (id == NO_ID) return null

        nameCache[id]?.let { return it.ifEmpty { null } }

        val name =
            runCatching {
                resources.getResourceEntryName(id)
            }.getOrDefault("")

        nameCache[id] = name
        name?.let { idCache[it] = id }

        return name.ifEmpty { null }
    }
}