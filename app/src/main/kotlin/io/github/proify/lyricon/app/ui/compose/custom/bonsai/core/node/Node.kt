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

package io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.BonsaiScope
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node.extension.ExpandableNode
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node.extension.ExpandableNodeHandler
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node.extension.SelectableNode
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node.extension.SelectableNodeHandler
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.util.randomUUID

typealias NodeComponent<T> = @Composable BonsaiScope<T>.(Node<T>) -> Unit

sealed interface Node<T> {
    val backgroundColor: MutableState<Color>

    val key: String

    val content: T

    val name: String
    val secondary: String?

    val depth: Int

    val isSelected: Boolean

    val iconComponent: NodeComponent<T>

    val nameComponent: NodeComponent<T>
}

class LeafNode<T> internal constructor(
    override val content: T,
    override val depth: Int,
    override val key: String = randomUUID,
    override val name: String = content.toString(),
    override val secondary: String?,
    override val iconComponent: NodeComponent<T> = { DefaultNodeIcon(it) },
    override val nameComponent: NodeComponent<T> = { DefaultNodeName(it) },
    override val backgroundColor: MutableState<Color>
) : Node<T>,
    SelectableNode by SelectableNodeHandler()

class BranchNode<T> internal constructor(
    override val content: T,
    override val depth: Int,
    override val key: String = randomUUID,
    override val name: String = content.toString(),
    override val secondary: String?,
    override val iconComponent: NodeComponent<T> = { DefaultNodeIcon(it) },
    override val nameComponent: NodeComponent<T> = { DefaultNodeName(it) },
    override val backgroundColor: MutableState<Color>
) : Node<T>,
    SelectableNode by SelectableNodeHandler(),
    ExpandableNode by ExpandableNodeHandler()