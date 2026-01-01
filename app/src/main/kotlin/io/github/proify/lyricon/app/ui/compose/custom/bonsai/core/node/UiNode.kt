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

package io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.node

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import io.github.proify.lyricon.app.ui.compose.custom.bonsai.core.BonsaiScope

@Composable
internal fun <T> BonsaiScope<T>.Node(
    node: Node<T>
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(
                start = 16.dp + node.depth * style.toggleIconSize, end = 16.dp
            )
    ) {
        ToggleIcon(node)
        NodeContent(node)
    }
}

@Composable
private fun <T> BonsaiScope<T>.ToggleIcon(
    node: Node<T>
) {
    val toggleIcon = style.toggleIcon(node) ?: return

    if (node is BranchNode) {
        val rotationDegrees by animateFloatAsState(
            if (node.isExpanded) style.toggleIconRotationDegrees else 0f
        )

        Image(
            painter = toggleIcon,
            contentDescription = if (node.isExpanded) "Collapse node" else "Expand node",
            colorFilter = style.toggleIconColorFilter,
            modifier = Modifier
                .clip(style.toggleShape)
                .clickable { expandableManager.toggleExpansion(node) }
                .size(style.nodeIconSize)
                .requiredSize(style.toggleIconSize)
                .rotate(rotationDegrees)
        )
    } else {
        Spacer(Modifier.size(style.nodeIconSize))
    }
}

@Composable
private fun <T> BonsaiScope<T>.NodeContent(
    node: Node<T>
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(node.backgroundColor.value, style.nodeShape)
            .defaultMinSize(200.dp)
            .fillMaxHeight()
            .run {
                if (node.isSelected.not()) clip(style.nodeShape)
                else background(style.nodeSelectedBackgroundColor, style.nodeShape)
            }
            .then(clickableNode(node))
            .padding(style.nodePadding)
        //.requiredHeight(style.nodeIconSize)
    ) {
        with(node) {
            iconComponent(node)
            nameComponent(node)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T> BonsaiScope<T>.clickableNode(
    node: Node<T>
): Modifier =
    if (onClick == null && onLongClick == null && onDoubleClick == null) {
        Modifier // no click action, return a noop modifier
    } else if (onLongClick == null && onDoubleClick == null) {
        Modifier.clickable { onClick?.invoke(node) }
    } else {
        Modifier.combinedClickable(
            onClick = { onClick?.invoke(node) },
            onDoubleClick = { onDoubleClick?.invoke(node) },
            onLongClick = { onLongClick?.invoke(node) }
        )
    }

@Composable
internal fun <T> BonsaiScope<T>.DefaultNodeIcon(node: Node<T>) {
    val (icon, colorFilter) = if (node is BranchNode && node.isExpanded) {
        style.nodeExpandedIcon(node) to style.nodeExpandedIconColorFilter
    } else {
        style.nodeCollapsedIcon(node) to style.nodeCollapsedIconColorFilter
    }

    if (icon != null) {
        Image(
            painter = icon,
            colorFilter = colorFilter,
            contentDescription = node.name,
        )
    }
}

@Composable
internal fun <T> BonsaiScope<T>.DefaultNodeName(node: Node<T>) {
    if (node.secondary.isNullOrBlank()) {
        BasicText(
            text = node.name,
            style = style.nodeNameTextStyle,
            modifier = Modifier
                .padding(
                    start = style.nodeNameStartPadding,
                )
                .fillMaxWidth()
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = style.nodeNameStartPadding,
                )
        ) {
            BasicText(
                text = node.name,
                style = style.nodeNameTextStyle,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(Modifier.height(2.dp))
            BasicText(
                text = node.secondary!!,
                style = style.nodeSecondaryTextStyle,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

}