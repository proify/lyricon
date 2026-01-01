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

package io.github.proify.lyricon.app.ui.compose.custom.miuix.extra

// Copyright 2025, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.BasicComponent
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.BasicComponentColors
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.CheckboxColors
import top.yukonga.miuix.kmp.basic.CheckboxDefaults

/**
 * A checkbox with a title and a summary.
 *
 * @param title The title of the [SuperCheckbox].
 * @param checked The checked state of the [SuperCheckbox].
 * @param onCheckedChange The callback when the checked state of the [SuperCheckbox] is changed.
 * @param modifier The modifier to be applied to the [SuperCheckbox].
 * @param titleColor The color of the title.
 * @param summary The summary of the [SuperCheckbox].
 * @param summaryColor The color of the summary.
 * @param checkboxColors The [CheckboxColors] of the [SuperCheckbox].
 * @param rightActions The [Composable] content that on the right side of the [SuperCheckbox].
 * @param checkboxLocation The insertionOrder of checkbox, [CheckboxLocation.Left] or [CheckboxLocation.Right].
 * @param insideMargin The margin inside the [SuperCheckbox].
 * @param onClick Optional callback when the component is clicked before checkbox is toggled.
 * @param holdDownState Used to determine whether it is in the pressed state.
 * @param enabled Whether the [SuperCheckbox] is clickable.
 */
@Composable
fun SuperCheckbox(
    title: String,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    titleColor: BasicComponentColors = BasicComponentDefaults.titleColor(),
    summary: String? = null,
    summaryColor: BasicComponentColors = BasicComponentDefaults.summaryColor(),
    checkboxColors: CheckboxColors = CheckboxDefaults.checkboxColors(),
    leftAction: @Composable () -> Unit = {},
    rightActions: @Composable RowScope.() -> Unit = {},
    checkboxLocation: CheckboxLocation = CheckboxLocation.Left,
    insideMargin: PaddingValues = BasicComponentDefaults.InsideMargin,
    onClick: (() -> Unit)? = null,
    holdDownState: Boolean = false,
    enabled: Boolean = true
) {

    BasicComponent(
        modifier = modifier,
        insideMargin = insideMargin,
        title = title,
        titleColor = titleColor,
        summary = summary,
        summaryColor = summaryColor,
        leftAction = {
            if (checkboxLocation == CheckboxLocation.Left) {
                SuperCheckboxLeftAction(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    enabled = enabled,
                    checkboxColors = checkboxColors,
                    insideMargin = insideMargin
                )
            }
            leftAction()
        },
        rightActions = {
            SuperCheckboxRightActions(
                rightActions = rightActions,
                checkboxLocation = checkboxLocation,
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                checkboxColors = checkboxColors
            )
        },
        onClick = {
            if (enabled) {
                onClick?.invoke()
                onCheckedChange?.invoke(!checked)
            }
        },
        holdDownState = holdDownState,
        enabled = enabled
    )
}

@Composable
private fun SuperCheckboxLeftAction(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    enabled: Boolean,
    checkboxColors: CheckboxColors,
    insideMargin: PaddingValues
) {
    Checkbox(
        modifier = Modifier.padding(end = insideMargin.calculateLeftPadding(LayoutDirection.Ltr)),
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        colors = checkboxColors
    )
}

@Composable
private fun RowScope.SuperCheckboxRightActions(
    rightActions: @Composable RowScope.() -> Unit,
    checkboxLocation: CheckboxLocation,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    enabled: Boolean,
    checkboxColors: CheckboxColors
) {
    rightActions()
    if (checkboxLocation == CheckboxLocation.Right) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = checkboxColors
        )
    }
}

enum class CheckboxLocation {
    Left,
    Right,
}