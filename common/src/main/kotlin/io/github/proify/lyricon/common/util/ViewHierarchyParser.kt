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