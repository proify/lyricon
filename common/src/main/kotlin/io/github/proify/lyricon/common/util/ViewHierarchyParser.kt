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

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

object ViewHierarchyParser {
    private val resourceNameCache = ConcurrentHashMap<Int, String?>()

    fun buildNodeTree(viewGroup: ViewGroup): ViewTreeNode {
        val name = viewGroup.javaClass.name

        var children = mutableListOf<ViewTreeNode>()

        viewGroup.forEach {
            createNodeFromView(it)?.let {
                children.add(it)
            }
        }

        return ViewTreeNode(
            id = getResourceName(viewGroup),
            name = name,
            children = children,
            view = WeakReference<View>(viewGroup)
        )
    }

    private fun createNodeFromView(view: View): ViewTreeNode? {
        return if (view is ViewGroup) {
            buildNodeTree(view)
        } else {
            val name = view.javaClass.name
            ViewTreeNode(
                id = getResourceName(view),
                name = name,
                view = WeakReference<View>(view)
            )
        }
    }

    fun getResourceName(
        view: View,
        resources: Resources = view.resources
    ): String? {
        val id = view.id
        if (id == View.NO_ID) return null

        if (resourceNameCache.containsKey(id)) return resourceNameCache[id]

        val name = runCatching {
            resources.getResourceEntryName(id)
        }.getOrNull()

        resourceNameCache[id] = name
        return name
    }
}