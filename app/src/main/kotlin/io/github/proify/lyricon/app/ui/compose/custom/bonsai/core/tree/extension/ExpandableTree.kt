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
