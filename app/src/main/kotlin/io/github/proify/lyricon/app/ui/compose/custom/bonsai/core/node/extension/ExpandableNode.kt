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
