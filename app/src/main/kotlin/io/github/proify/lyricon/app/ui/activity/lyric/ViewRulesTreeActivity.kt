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

import android.app.Activity
import android.view.HapticFeedbackConstants
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node.Node
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.Card
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.SuperCheckbox
import io.github.proify.lyricon.app.util.LyricPrefs
import io.github.proify.lyricon.common.util.ViewTreeNode
import io.github.proify.lyricon.lyric.style.VisibilityRule
import top.yukonga.miuix.kmp.extra.SuperBottomSheet
import top.yukonga.miuix.kmp.theme.MiuixTheme

private data class VisibilityOption(
    val mode: Int,
    val titleRes: Int
)

class ViewRulesTreeActivity : ViewTreeActivity() {

    private val viewModel: RuleViewModel by viewModels()

    private val activeRuleColor = Color(color = 0xFF66BB6A)

    override fun getToolBarTitle(): String = getString(R.string.activity_view_rules)

    override fun createViewModel() = object : ViewTreeViewModel() {
        override fun handleNodeClick(node: Node<ViewTreeNode>) {
            val nodeId = node.content.id ?: return
            viewModel.openSelection(nodeId)
        }

        override fun getNodeColor(node: ViewTreeNode): Color {
            val rules = LyricPrefs.getViewVisibilityRule()
            val hasActiveRule =
                rules.any { it.id == node.id && it.mode != VisibilityRule.MODE_NORMAL }
            return if (hasActiveRule) activeRuleColor else Color.Transparent
        }
    }

    @Composable
    override fun OnScaffoldCreated() {
        super.OnScaffoldCreated()

        val currentMode by viewModel.currentMode

        VisibilityRuleBottomSheet(
            show = viewModel.showOptions,
            nodeId = viewModel.editId.value,
            selectedMode = currentMode,
            onModeSelected = { newMode ->
                viewModel.updateRule(newMode)
                refreshTreeDisplay()
            }
        )
    }

    class RuleViewModel : ViewModel() {
        val showOptions = mutableStateOf(false)
        val editId = mutableStateOf("")

        private val _currentMode = mutableIntStateOf(VisibilityRule.MODE_NORMAL)
        val currentMode: State<Int> = _currentMode

        fun openSelection(id: String) {
            editId.value = id
            val rules = LyricPrefs.getViewVisibilityRule()
            _currentMode.intValue = rules.find { it.id == id }?.mode ?: VisibilityRule.MODE_NORMAL
            showOptions.value = true
        }

        fun updateRule(newMode: Int) {
            val rules = LyricPrefs.getViewVisibilityRule().toMutableList()
            val existingIndex = rules.indexOfFirst { it.id == editId.value }

            if (existingIndex != -1) {
                rules[existingIndex] = VisibilityRule(id = editId.value, mode = newMode)
            } else {
                rules.add(VisibilityRule(id = editId.value, mode = newMode))
            }

            LyricPrefs.setViewVisibilityRule(rules)
            _currentMode.intValue = newMode
            showOptions.value = false
        }
    }
}

@Composable
private fun VisibilityRuleBottomSheet(
    show: MutableState<Boolean>,
    nodeId: String,
    selectedMode: Int,
    onModeSelected: (Int) -> Unit
) {
    val options = remember {
        listOf(
            VisibilityOption(VisibilityRule.MODE_NORMAL, R.string.option_visibility_default),
            VisibilityOption(
                VisibilityRule.MODE_ALWAYS_VISIBLE,
                R.string.option_visibility_always_visible
            ),
            VisibilityOption(
                VisibilityRule.MODE_ALWAYS_HIDDEN,
                R.string.option_visibility_always_hidden
            ),
            VisibilityOption(
                VisibilityRule.MODE_HIDE_WHEN_PLAYING,
                R.string.option_visibility_hide_when_playing
            )
        )
    }

    val context = LocalContext.current

    SuperBottomSheet(
        backgroundColor = MiuixTheme.colorScheme.surface,
        title = nodeId,
        show = show,
        onDismissRequest = { show.value = false },
        insideMargin = DpSize(16.dp, 0.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            options.forEach { option ->
                SuperCheckbox(
                    title = stringResource(option.titleRes),
                    checked = selectedMode == option.mode,
                    onCheckedChange = { isChecked ->
                        if (isChecked && selectedMode != option.mode) {
                            onModeSelected(option.mode)
                            (context as? Activity)?.window?.decorView?.performHapticFeedback(
                                HapticFeedbackConstants.TOGGLE_ON
                            )
                        }
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}