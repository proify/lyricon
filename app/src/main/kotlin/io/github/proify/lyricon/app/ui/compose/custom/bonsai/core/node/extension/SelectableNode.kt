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

internal interface SelectableNode {

    val isSelected: Boolean

    var isSelectedState: Boolean

    var onToggleSelected: (Boolean) -> Unit

    fun setSelected(isSelected: Boolean)
}

internal class SelectableNodeHandler : SelectableNode {

    override val isSelected: Boolean
        get() = isSelectedState

    override var isSelectedState: Boolean by mutableStateOf(false)

    override var onToggleSelected: (Boolean) -> Unit by mutableStateOf({})

    override fun setSelected(isSelected: Boolean) {
        onToggleSelected(isSelected)
    }
}
