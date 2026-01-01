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

package io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.tree.extension

import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node.BranchNode
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node.Node

interface ExpandableTree<T> {

    fun toggleExpansion(node: Node<T>)

    fun collapseRoot()

    fun expandRoot()

    fun collapseAll()

    fun expandAll()

    fun collapseFrom(depth: Int)

    fun expandUntil(depth: Int)

    fun collapseNode(node: Node<T>)

    fun expandNode(node: Node<T>)
}

internal class ExpandableTreeHandler<T>(
    private val nodes: List<Node<T>>
) : ExpandableTree<T> {

    override fun toggleExpansion(node: Node<T>) {
        if (node !is BranchNode) return

        if (node.isExpanded) collapseNode(node)
        else expandNode(node)
    }

    override fun collapseRoot() {
        collapse(nodes, depth = 0)
    }

    override fun expandRoot() {
        expand(nodes, depth = 0)
    }

    override fun collapseAll() {
        collapse(nodes, depth = 0)
    }

    override fun expandAll() {
        expand(nodes, depth = Int.MAX_VALUE)
    }

    override fun collapseFrom(depth: Int) {
        collapse(nodes, depth)
    }

    override fun expandUntil(depth: Int) {
        expand(nodes, depth)
    }

    override fun collapseNode(node: Node<T>) {
        collapse(listOf(node), node.depth)
    }

    override fun expandNode(node: Node<T>) {
        expand(listOf(node), node.depth)
    }

    private fun collapse(nodes: List<Node<T>>, depth: Int) {
        nodes.asSequence()
            .filterIsInstance<BranchNode<T>>()
            .filter { it.depth >= depth }
            .sortedByDescending { it.depth }
            .forEach { it.setExpanded(false, depth) }
    }

    private fun expand(nodes: List<Node<T>>, depth: Int) {
        nodes.asSequence()
            .filterIsInstance<BranchNode<T>>()
            .filter { it.depth <= depth }
            .sortedBy { it.depth }
            .forEach { it.setExpanded(true, depth) }
    }
}
