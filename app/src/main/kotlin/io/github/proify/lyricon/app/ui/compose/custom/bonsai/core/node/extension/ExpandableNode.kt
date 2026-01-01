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

package io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node.extension

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal interface ExpandableNode {

    val isExpanded: Boolean

    var isExpandedState: Boolean

    var onToggleExpanded: (Boolean, Int) -> Unit

    fun setExpanded(isExpanded: Boolean, maxDepth: Int)
}

internal class ExpandableNodeHandler : ExpandableNode {

    override val isExpanded: Boolean
        get() = isExpandedState

    override var isExpandedState: Boolean by mutableStateOf(false)

    override var onToggleExpanded: (Boolean, Int) -> Unit by mutableStateOf({ _, _ -> })

    override fun setExpanded(isExpanded: Boolean, maxDepth: Int) {
        onToggleExpanded(isExpanded, maxDepth)
    }
}
