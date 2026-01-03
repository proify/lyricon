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