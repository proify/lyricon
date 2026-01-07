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

import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import io.github.proify.lyricon.common.util.ResourceMapper.getIdName
import java.lang.ref.WeakReference

object ViewHierarchyParser {
    fun buildNodeTree(viewGroup: ViewGroup): ViewTreeNode {
        val name = viewGroup.javaClass.name

        val children = mutableListOf<ViewTreeNode>()

        viewGroup.forEach { view ->
            createNodeFromView(view)?.let { node ->
                children.add(node)
            }
        }

        return ViewTreeNode(
            id = getResourceName(viewGroup),
            name = name,
            children = children,
            view = WeakReference(viewGroup),
        )
    }

    private fun createNodeFromView(view: View): ViewTreeNode? =
        if (view is ViewGroup) {
            buildNodeTree(view)
        } else {
            val name = view.javaClass.name
            ViewTreeNode(
                id = getResourceName(view),
                name = name,
                view = WeakReference(view),
            )
        }

    fun getResourceName(view: View): String? = getIdName(view)
}