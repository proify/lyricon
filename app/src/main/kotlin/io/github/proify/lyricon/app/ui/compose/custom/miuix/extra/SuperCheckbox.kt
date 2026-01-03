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