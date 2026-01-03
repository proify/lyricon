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
import android.view.HapticFeedbackConstants
import androidx.compose.ui.graphics.Color
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node.Node
import io.github.proify.lyricon.app.util.LyricPrefs
import io.github.proify.lyricon.app.util.Utils.commitEdit
import io.github.proify.lyricon.common.util.ViewTreeNode
import io.github.proify.lyricon.lyric.style.BasicStyle

class AnchorViewTreeActivity : ViewTreeActivity() {
    private val preferences by lazy { LyricPrefs.basicStylePrefs }
    private var currentAnchor: String = BasicStyle.Defaults.ANCHOR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences.registerOnSharedPreferenceChangeListener(this)
        currentAnchor =
            preferences.getString("lyric_style_base_anchor", currentAnchor) ?: currentAnchor
    }

    override fun getToolBarTitle(): String = getString(R.string.activity_anchor)

    override fun onDestroy() {
        super.onDestroy()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun createViewModel() = object : ViewTreeViewModel() {
        override fun handleNodeClick(node: Node<ViewTreeNode>) {
            val value = node.content
            val id = value.id ?: return
            if (id == "status_bar" || id == currentAnchor) return

            preferences.commitEdit { putString("lyric_style_base_anchor", id) }
            currentAnchor = id
            refreshTreeDisplay()

            window.decorView.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }

        override fun getNodeColor(node: ViewTreeNode): Color =
            when (node.id) {
                currentAnchor -> Color(color = 0xFF66bb6a)
                else -> Color.Transparent
            }
    }

}