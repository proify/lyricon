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

package io.github.proify.lyricon.app.ui.activity.lyric

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.github.proify.android.extensions.inflate
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.bridge.BridgeConstants
import io.github.proify.lyricon.app.ui.compose.BlurTopAppBar
import io.github.proify.lyricon.app.ui.compose.NavigationBackIcon
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.Bonsai
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.BonsaiStyle
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node.Branch
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node.Leaf
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node.Node
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.tree.Tree
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.tree.TreeScope
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.MiuixScrollBehavior
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.ScrollBehavior
import io.github.proify.lyricon.app.ui.theme.AppTheme
import io.github.proify.lyricon.common.util.ViewTreeNode
import kotlinx.serialization.json.Json
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

abstract class BaseViewTreeActivity : BaseLyricActivity() {

    private var isTreeLoading by mutableStateOf(true)
    private var viewTreeJson by mutableStateOf(EMPTY_JSON)
    private val nodeColorStateCache = mutableMapOf<String, MutableState<Color>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ViewTreeContent(viewTreeJson)
        }
        setupViewTreeDataChannel()
    }

    private fun setupViewTreeDataChannel() {
        systemUIChannel.wait<ByteArray>(key = BridgeConstants.REQUEST_VIEW_TREE_CALLBACK) { compressedData ->
            viewTreeJson = compressedData.inflate()
            isTreeLoading = false
        }

        systemUIChannel.put(BridgeConstants.REQUEST_VIEW_TREE)
    }

    protected fun refreshTreeDisplay() {
        updateAllNodeColors()
    }

    private fun updateAllNodeColors() {
        nodeColorStateCache.forEach { (nodeId, colorState) ->
            val node = ViewTreeNode(id = nodeId, name = null)
            colorState.value = computeNodeColor(node)
        }
    }

    private fun getOrCreateNodeColorState(nodeId: String, node: ViewTreeNode): MutableState<Color> {
        return nodeColorStateCache.getOrPut(nodeId) {
            mutableStateOf(computeNodeColor(node))
        }
    }

    private fun computeNodeColor(node: ViewTreeNode): Color {
        return getViewTreeNodeColor(node) ?: Color.Companion.Transparent
    }

    abstract fun getToolBarTitle(): String

    @Composable
    private fun ViewTreeContent(jsonData: String) {
        AppTheme {
            val scrollBehavior = MiuixScrollBehavior()
            val hazeState = rememberHazeState()

            Scaffold(
                modifier = Modifier.Companion.fillMaxSize(),
                topBar = {
                    ViewTreeTopBar(
                        title = getToolBarTitle(),
                        hazeState = hazeState,
                        scrollBehavior = scrollBehavior
                    )
                }
            ) { paddingValues ->
                OnScaffoldCreated()

                ViewTreeContentWithLoading(
                    jsonData = jsonData,
                    hazeState = hazeState,
                    scrollBehavior = scrollBehavior,
                    paddingValues = paddingValues,
                    isLoading = isTreeLoading
                )
            }
        }
    }

    @Composable
    private fun ViewTreeTopBar(
        title: String,
        hazeState: HazeState,
        scrollBehavior: ScrollBehavior
    ) {
        BlurTopAppBar(
            hazeState = hazeState,
            navigationIcon = { NavigationBackIcon() },
            title = title,
            scrollBehavior = scrollBehavior
        )
    }

    @Composable
    private fun ViewTreeContentWithLoading(
        jsonData: String,
        hazeState: HazeState,
        scrollBehavior: ScrollBehavior,
        paddingValues: PaddingValues,
        isLoading: Boolean
    ) {
        Box(modifier = Modifier.Companion.fillMaxSize()) {
            ViewTreeList(
                jsonData = jsonData,
                hazeState = hazeState,
                scrollBehavior = scrollBehavior,
                paddingValues = paddingValues
            )

            if (isLoading) {
                LoadingIndicator(paddingValues)
            }
        }
    }

    @Composable
    private fun LoadingIndicator(paddingValues: PaddingValues) {
        Box(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            contentAlignment = Alignment.Companion.Center
        ) {
            CircularProgressIndicator()
        }
    }

    @Composable
    private fun ViewTreeList(
        jsonData: String,
        hazeState: HazeState,
        scrollBehavior: ScrollBehavior,
        paddingValues: PaddingValues
    ) {
        val tree = createTreeFromJson(jsonData)

        LaunchedEffect(jsonData) {
            tree.expandAll()
        }

        Bonsai(
            modifier = Modifier.Companion
                .hazeSource(hazeState)
                .fillMaxSize()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = 0.dp,
                    end = 0.dp
                ),
            tree = tree,
            onClick = ::onTreeNodeClick,
            onDoubleClick = null,
            onLongClick = null,
            style = createBonsaiStyle()
        )
    }

    @Composable
    private fun createBonsaiStyle(): BonsaiStyle<ViewTreeNode> {
        return BonsaiStyle(
            toggleIcon = { rememberVectorPainter(Icons.AutoMirrored.Rounded.KeyboardArrowRight) },
            toggleIconColorFilter = ColorFilter.Companion.tint(MiuixTheme.colorScheme.onBackground),
            nodePadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
            toggleIconSize = 20.dp,
            toggleIconRotationDegrees = 45f,
            nodeNameTextStyle = TextStyle(
                fontWeight = FontWeight.Companion.Medium,
                fontSize = 16.sp,
                color = MiuixTheme.colorScheme.onBackground
            ),
            nodeSecondaryTextStyle = TextStyle(
                fontWeight = FontWeight.Companion.Medium,
                fontSize = 14.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )
        )
    }

    @Composable
    private fun createTreeFromJson(jsonData: String): Tree<ViewTreeNode> {
        return Tree {
            runCatching {
                val rootNode = Json.Default.decodeFromString<ViewTreeNode>(jsonData)
                TreeNode(node = rootNode)
            }
        }
    }

    @Composable
    private fun TreeScope.TreeNode(node: ViewTreeNode) {
        val nodeId = node.id ?: generateNodeId(node)
        val colorState = remember(nodeId) {
            getOrCreateNodeColorState(nodeId, node)
        }
        val displayName = simplifyNodeName(node.name)

        if (node.children.isEmpty()) {
            RenderLeafNode(
                node = node,
                displayName = displayName,
                colorState = colorState
            )
        } else {
            RenderBranchNode(
                node = node,
                displayName = displayName,
                colorState = colorState
            )
        }
    }

    @Composable
    private fun TreeScope.RenderLeafNode(
        node: ViewTreeNode,
        displayName: String,
        colorState: MutableState<Color>
    ) {
        Leaf(
            backgroundColor = colorState,
            content = node,
            name = displayName,
            secondary = node.id,
            customIcon = {
                LeafNodeIcon()
            }
        )
    }

    @Composable
    private fun TreeScope.RenderBranchNode(
        node: ViewTreeNode,
        displayName: String,
        colorState: MutableState<Color>
    ) {
        Branch(
            backgroundColor = colorState,
            content = node,
            name = displayName,
            secondary = node.id,
        ) {
            node.children.forEach { childNode ->
                TreeNode(node = childNode)
            }
        }
    }

    @Composable
    private fun LeafNodeIcon() {
        Icon(
            modifier = Modifier.Companion.size(14.dp),
            painter = painterResource(R.drawable.ic_view_stream),
            tint = MiuixTheme.colorScheme.onBackground,
            contentDescription = null,
        )
        Spacer(Modifier.Companion.width(6.dp))
    }

    private fun generateNodeId(node: ViewTreeNode): String {
        return "node_${node.hashCode()}"
    }

    private fun simplifyNodeName(fullName: String?): String {
        return fullName
            ?.replace("android.widget.", "")
            ?.replace("android.view.", "")
            ?: "Unknown View"
    }

    @Composable
    protected open fun OnScaffoldCreated() {
    }

    protected abstract fun onTreeNodeClick(node: Node<ViewTreeNode>)

    protected open fun getViewTreeNodeColor(node: ViewTreeNode): Color? = null

    @Preview(showBackground = true)
    @Composable
    private fun ViewTreeContentPreview() {
        ViewTreeContent(EMPTY_JSON)
    }

    private companion object {
        const val EMPTY_JSON = "{}"
    }
}