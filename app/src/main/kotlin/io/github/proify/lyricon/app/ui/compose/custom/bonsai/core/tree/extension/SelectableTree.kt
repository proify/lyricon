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
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node.LeafNode
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node.Node

interface SelectableTree<T> {

    val selectedNodes: List<Node<T>>

    fun toggleSelection(node: Node<T>)

    fun selectNode(node: Node<T>)

    fun unselectNode(node: Node<T>)

    fun clearSelection()
}

internal class SelectableTreeHandler<T>(
    private val nodes: List<Node<T>>
) : SelectableTree<T> {

    override val selectedNodes: List<Node<T>>
        get() = nodes.filter { it.isSelected }

    override fun toggleSelection(node: Node<T>) {
        if (node.isSelected) unselectNode(node)
        else selectNode(node)
    }

    override fun selectNode(node: Node<T>) {
        node.setSelected(true)
    }

    override fun unselectNode(node: Node<T>) {
        node.setSelected(false)
    }

    override fun clearSelection() {
        selectedNodes.forEach { it.setSelected(false) }
    }

    private fun Node<T>.setSelected(isSelected: Boolean) {
        when (this) {
            is LeafNode -> setSelected(isSelected)
            is BranchNode -> setSelected(isSelected)
        }
    }
}
