package io.github.proify.lyricon.app.ui.activity.lyric.tree

import androidx.activity.viewModels
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node.Node
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.Card
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.SuperCheckbox
import io.github.proify.lyricon.app.util.LyricPrefs
import io.github.proify.lyricon.common.util.ViewTreeNode
import io.github.proify.lyricon.lyric.style.VisibilityRule
import top.yukonga.miuix.kmp.extra.SuperBottomSheet
import top.yukonga.miuix.kmp.theme.MiuixTheme

class ViewRulesTreeActivity : ViewTreeActivity() {

    private val viewModel: ActivityViewModel by viewModels()

    class ActivityViewModel : ViewModel() {
        val showOptions = mutableStateOf(false)
        val editId = mutableStateOf("")
    }

    override fun getViewTreeNodeColor(node: ViewTreeNode): Color? {
        val rules = LyricPrefs.getViewVisibilityRule()
        return if (rules.any { it.id == node.id && it.mode != VisibilityRule.MODE_NORMAL }) {
            Color(0xFF66bb6a)
        } else {
            null
        }
    }

    override fun onTreeNodeClick(node: Node<ViewTreeNode>) {
        val id = node.content.id ?: return
        viewModel.editId.value = id
        viewModel.showOptions.value = true

//        window.decorView.performHapticFeedback(
//            android.view.HapticFeedbackConstants.CONTEXT_CLICK
//        )
    }

    @Composable
    override fun OnScaffoldCreated() {
        super.OnScaffoldCreated()

        val items = listOf(
            "默认",
            "总是显示",
            "总是隐藏",
            "播放时隐藏"
        )
        val itemValues = listOf(
            VisibilityRule.MODE_NORMAL,
            VisibilityRule.MODE_ALWAYS_VISIBLE,
            VisibilityRule.MODE_ALWAYS_HIDDEN,
            VisibilityRule.MODE_HIDE_WHEN_PLAYING
        )

        var checkedIndex by remember { mutableIntStateOf(-1) }

        LaunchedEffect(viewModel.showOptions.value, viewModel.editId.value) {
            if (viewModel.showOptions.value) {
                val rules = LyricPrefs.getViewVisibilityRule()
                val currentRule = rules.find { it.id == viewModel.editId.value }
                checkedIndex = if (currentRule != null) {
                    itemValues.indexOf(currentRule.mode)
                } else {
                    0
                }
            }
        }

        SuperBottomSheet(
            backgroundColor = MiuixTheme.colorScheme.surface,
            title = viewModel.editId.value,
            show = viewModel.showOptions,
            onDismissRequest = { viewModel.showOptions.value = false },
            insideMargin = DpSize(16.dp, 0.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                items.forEachIndexed { index, item ->
                    SuperCheckbox(
                        title = item,
                        checked = checkedIndex == index,
                        onCheckedChange = {
                            if (checkedIndex == index) {
                                return@SuperCheckbox
                            }
                            checkedIndex = index
                            val rules = LyricPrefs.getViewVisibilityRule().toMutableList()
                            val rule = rules.find { it.id == viewModel.editId.value }
                            if (rule != null) {
                                rule.mode = itemValues[index]
                            } else {
                                rules.add(
                                    VisibilityRule(
                                        id = viewModel.editId.value,
                                        mode = itemValues[index]
                                    )
                                )
                            }
                            LyricPrefs.setViewVisibilityRule(rules)
                            viewModel.showOptions.value = false
                            refreshTreeDisplay()

                            window.decorView.performHapticFeedback(
                                android.view.HapticFeedbackConstants.TOGGLE_ON
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}