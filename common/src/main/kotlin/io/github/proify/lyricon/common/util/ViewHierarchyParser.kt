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