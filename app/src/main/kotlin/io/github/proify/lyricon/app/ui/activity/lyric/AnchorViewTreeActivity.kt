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
import android.view.HapticFeedbackConstants
import androidx.compose.ui.graphics.Color
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node.Node
import io.github.proify.lyricon.app.util.LyricPrefs
import io.github.proify.lyricon.app.util.Utils.commitEdit
import io.github.proify.lyricon.common.util.ViewTreeNode
import io.github.proify.lyricon.lyric.style.BasicStyle

class AnchorViewTreeActivity : BaseViewTreeActivity() {
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

    override fun onTreeNodeClick(node: Node<ViewTreeNode>) {
        val value = node.content
        val id = value.id ?: return
        if (id == "status_bar" || id == currentAnchor) return

        preferences.commitEdit { putString("lyric_style_base_anchor", id) }
        currentAnchor = id
        refreshTreeDisplay()

        window.decorView.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }

    override fun getViewTreeNodeColor(node: ViewTreeNode): Color? {
        return when (node.id) {
            currentAnchor -> Color(0xFF66bb6a)
            else -> null
        }
    }
}