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

package io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.tree

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node.Node
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node.TreeApplier
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.tree.extension.ExpandableTree
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.tree.extension.ExpandableTreeHandler
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.tree.extension.SelectableTree
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.tree.extension.SelectableTreeHandler

@DslMarker
private annotation class TreeMarker

@Immutable
@TreeMarker
data class TreeScope(
    val depth: Int,
    internal val isExpanded: Boolean = false,
    internal val expandMaxDepth: Int = 0
)

@Stable
class Tree<T> internal constructor(
    val nodes: List<Node<T>>
) : ExpandableTree<T> by ExpandableTreeHandler(nodes),
    SelectableTree<T> by SelectableTreeHandler(nodes)

@SuppressLint("ComposableNaming")
@Composable
fun <T> Tree(
    content: @Composable TreeScope.() -> Unit
): Tree<T> {
    val applier = remember { TreeApplier<T>() }
    val compositionContext = rememberCompositionContext()
    val composition =
        remember(applier, compositionContext) { Composition(applier, compositionContext) }
    composition.setContent { TreeScope(depth = 0).content() }
    return remember(applier) { Tree(applier.children) }
}