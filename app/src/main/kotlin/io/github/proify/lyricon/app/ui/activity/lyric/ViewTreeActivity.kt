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

package io.github.proify.lyricon.app.ui.activity.lyric

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.github.proify.android.extensions.inflate
import io.github.proify.lyricon.app.Application.Companion.systemUIChannel
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.bridge.AppBridgeConstants
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

abstract class ViewTreeActivity : BaseLyricActivity() {

    // ==================== UI State & Event ====================

    /**
     * UI 状态的单一数据源
     */
    data class ViewTreeUiState(
        val isLoading: Boolean = true,
        val viewTreeData: ViewTreeNode? = null,
        val error: String? = null,
        val nodeColors: Map<String, Color> = emptyMap()
    )

    /**
     * UI 事件
     */
    sealed interface ViewTreeUiEvent {
        data class NodeClicked(val node: Node<ViewTreeNode>) : ViewTreeUiEvent
        data object RefreshRequested : ViewTreeUiEvent
        data object RetryLoading : ViewTreeUiEvent
    }

    // ==================== Repository ====================

    /**
     * Repository 负责数据获取和缓存
     */
    inner class ViewTreeRepository {
        private var cachedTree: ViewTreeNode? = null

        suspend fun getViewTree(): Result<ViewTreeNode> {
            return try {
                val tree = fetchViewTreeFromChannel()
                cachedTree = tree
                Result.success(tree)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        private suspend fun fetchViewTreeFromChannel(): ViewTreeNode =
            suspendCancellableCoroutine { continuation ->
                systemUIChannel.wait<ByteArray>(
                    key = AppBridgeConstants.REQUEST_VIEW_TREE_CALLBACK
                ) { compressedData ->
                    try {
                        val jsonData = compressedData.inflate()
                        val viewTree = Json.Default.decodeFromString<ViewTreeNode>(jsonData)
                        continuation.resume(viewTree)
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }

                systemUIChannel.put(AppBridgeConstants.REQUEST_VIEW_TREE)
            }
    }

    // ==================== ViewModel ====================

    abstract inner class ViewTreeViewModel : ViewModel() {
        private val repository = ViewTreeRepository()

        private val _uiState = MutableStateFlow(ViewTreeUiState())
        val uiState: StateFlow<ViewTreeUiState> = _uiState.asStateFlow()

        init {
            loadViewTree()
        }

        fun onEvent(event: ViewTreeUiEvent) {
            when (event) {
                is ViewTreeUiEvent.NodeClicked -> handleNodeClick(event.node)
                is ViewTreeUiEvent.RefreshRequested -> refreshViewTree()
                is ViewTreeUiEvent.RetryLoading -> loadViewTree()
            }
        }

        private fun loadViewTree() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }

                repository.getViewTree()
                    .onSuccess { viewTree ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                viewTreeData = viewTree,
                                nodeColors = computeAllNodeColors(viewTree)
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message
                            )
                        }
                    }
            }
        }

        fun refreshViewTree() {
            viewModelScope.launch {
                _uiState.value.viewTreeData?.let { currentTree ->
                    val updatedColors = computeAllNodeColors(currentTree)
                    _uiState.update { it.copy(nodeColors = updatedColors) }
                }
            }
        }

        private fun computeAllNodeColors(node: ViewTreeNode): Map<String, Color> {
            val colorMap = mutableMapOf<String, Color>()

            fun traverse(current: ViewTreeNode) {
                current.id?.let { id ->
                    colorMap[id] = getNodeColor(current)
                }
                current.children?.forEach { traverse(it) }
            }

            traverse(node)
            return colorMap
        }

        protected abstract fun handleNodeClick(node: Node<ViewTreeNode>)
        protected abstract fun getNodeColor(node: ViewTreeNode): Color
    }

    // ==================== Activity ====================

    protected abstract fun createViewModel(): ViewTreeViewModel

    private val viewModel: ViewTreeViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return createViewModel() as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ViewTreeScreen()
        }
    }

    /**
     * 提供给子类调用,刷新树的显示
     */
    protected fun refreshTreeDisplay() {
        viewModel.refreshViewTree()
    }

    // ==================== Compose UI ====================

    @Composable
    private fun ViewTreeScreen() {
        val uiState by viewModel.uiState.collectAsState()

        AppTheme {
            ViewTreeContent(uiState)
        }
    }

    @Composable
    private fun ViewTreeContent(uiState: ViewTreeUiState) {
        val scrollBehavior = MiuixScrollBehavior()
        val hazeState = rememberHazeState()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                ViewTreeTopBar(
                    title = getToolBarTitle(),
                    hazeState = hazeState,
                    scrollBehavior = scrollBehavior
                )
            }
        ) { paddingValues ->
            OnScaffoldCreated()

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.viewTreeData != null -> {
                        ViewTreeList(
                            viewTree = uiState.viewTreeData,
                            nodeColors = uiState.nodeColors,
                            hazeState = hazeState,
                            scrollBehavior = scrollBehavior,
                            paddingValues = paddingValues
                        )
                    }
                }

                if (uiState.isLoading) {
                    LoadingIndicator(paddingValues)
                }
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
    private fun LoadingIndicator(paddingValues: PaddingValues) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    @Composable
    private fun ViewTreeList(
        viewTree: ViewTreeNode,
        nodeColors: Map<String, Color>,
        hazeState: HazeState,
        scrollBehavior: ScrollBehavior,
        paddingValues: PaddingValues
    ) {
        val tree = Tree<ViewTreeNode> {
            TreeNode(node = viewTree)
        }

        LaunchedEffect(viewTree) {
            tree.expandAll()
        }

        Bonsai(
            modifier = Modifier
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
            onClick = { node ->
                viewModel.onEvent(ViewTreeUiEvent.NodeClicked(node))
            },
            onDoubleClick = null,
            onLongClick = null,
            style = createBonsaiStyle()
        )
    }

    @Composable
    private fun createBonsaiStyle(): BonsaiStyle<ViewTreeNode> {
        return BonsaiStyle(
            toggleIcon = { rememberVectorPainter(Icons.AutoMirrored.Rounded.KeyboardArrowRight) },
            toggleIconColorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onBackground),
            nodePadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
            toggleIconSize = 20.dp,
            toggleIconRotationDegrees = 45f,
            nodeNameTextStyle = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = MiuixTheme.colorScheme.onBackground
            ),
            nodeSecondaryTextStyle = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )
        )
    }

    @Composable
    private fun TreeScope.TreeNode(node: ViewTreeNode) {
        val uiState by viewModel.uiState.collectAsState()
        val nodeId = node.id ?: "node_${node.hashCode()}"
        val nodeColor = uiState.nodeColors[nodeId] ?: Color.Transparent
        val displayName = simplifyNodeName(node.name)

        if (node.children.isNullOrEmpty()) {
            RenderLeafNode(
                node = node,
                displayName = displayName,
                color = nodeColor
            )
        } else {
            RenderBranchNode(
                node = node,
                displayName = displayName,
                color = nodeColor
            )
        }
    }

    @Composable
    private fun TreeScope.RenderLeafNode(
        node: ViewTreeNode,
        displayName: String,
        color: Color
    ) {
        Leaf(
            backgroundColor = remember { mutableStateOf(color) }.apply { value = color },
            content = node,
            name = displayName,
            secondary = node.id,
            customIcon = {
                Icon(
                    modifier = Modifier.size(14.dp),
                    painter = painterResource(R.drawable.ic_view_stream),
                    tint = MiuixTheme.colorScheme.onBackground,
                    contentDescription = null,
                )
                Spacer(Modifier.width(6.dp))
            }
        )
    }

    @Composable
    private fun TreeScope.RenderBranchNode(
        node: ViewTreeNode,
        displayName: String,
        color: Color
    ) {
        Branch(
            backgroundColor = remember { mutableStateOf(color) }.apply { value = color },
            content = node,
            name = displayName,
            secondary = node.id,
        ) {
            node.children?.forEach { childNode ->
                TreeNode(node = childNode)
            }
        }
    }

    private fun simplifyNodeName(fullName: String?): String {
        return fullName
            ?.replace("android.widget.", "")
            ?.replace("android.view.", "")
            ?: "Unknown View"
    }

    protected abstract fun getToolBarTitle(): String

    @Composable
    protected open fun OnScaffoldCreated() {
    }
}