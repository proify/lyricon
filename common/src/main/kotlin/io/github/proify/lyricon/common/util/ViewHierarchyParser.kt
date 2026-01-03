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
import java.lang.ref.WeakReference

object ViewHierarchyParser {

    fun buildNodeTree(viewGroup: ViewGroup): ViewTreeNode {
        val name = viewGroup.javaClass.name
        val node =
            ViewTreeNode(getResourceName(viewGroup), name, WeakReference<View>(viewGroup))

        for (i in 0 until viewGroup.childCount) {
            createNodeFromView(viewGroup.getChildAt(i))?.let {
                node.addChild(it)
            }
        }

        return node
    }

    private fun createNodeFromView(view: View): ViewTreeNode? {
        return if (view is ViewGroup) {
            buildNodeTree(view)
        } else {
            val name = view.javaClass.name
            ViewTreeNode(getResourceName(view), name, WeakReference<View>(view))
        }
    }

    private fun getResourceName(view: View): String? {
        return if (view.id == View.NO_ID) null else try {
            view.resources.getResourceEntryName(view.id)
        } catch (_: Exception) {
            null
        }
    }

}