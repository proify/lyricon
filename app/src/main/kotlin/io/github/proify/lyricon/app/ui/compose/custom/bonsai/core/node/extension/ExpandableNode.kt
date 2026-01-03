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
