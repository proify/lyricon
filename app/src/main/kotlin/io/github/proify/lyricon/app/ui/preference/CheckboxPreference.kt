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

package io.github.proify.lyricon.app.ui.preference

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.BasicComponentColors
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.BasicComponentDefaults
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.CheckboxLocation
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.SuperCheckbox
import top.yukonga.miuix.kmp.basic.CheckboxColors
import top.yukonga.miuix.kmp.basic.CheckboxDefaults

@Composable
fun CheckboxPreference(
    sharedPreferences: SharedPreferences,
    key: String,
    title: String,
    defaultValue: Boolean = false,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    titleColor: BasicComponentColors = BasicComponentDefaults.titleColor(),
    summary: String? = null,
    summaryColor: BasicComponentColors = BasicComponentDefaults.summaryColor(),
    checkboxColors: CheckboxColors = CheckboxDefaults.checkboxColors(),
    leftAction: @Composable () -> Unit = {},
    rightActions: @Composable RowScope.() -> Unit = {},
    checkboxLocation: CheckboxLocation = CheckboxLocation.Right,
    insideMargin: PaddingValues = BasicComponentDefaults.InsideMargin,
    onClick: (() -> Unit)? = null,
    holdDownState: Boolean = false,
    enabled: Boolean = true
) {
    val checked = rememberBooleanPreference(sharedPreferences, key, defaultValue)

    val hapticFeedback = LocalHapticFeedback.current

    SuperCheckbox(
        title = title,
        checked = checked.value,
        onCheckedChange = {
            hapticFeedback.performHapticFeedback(
                if (it) HapticFeedbackType.ToggleOn
                else HapticFeedbackType.ToggleOff
            )
            checked.value = it
        },
        modifier = modifier,
        titleColor = titleColor,
        summary = summary,
        summaryColor = summaryColor,
        checkboxColors = checkboxColors,
        leftAction = leftAction,
        rightActions = rightActions,
        checkboxLocation = checkboxLocation,
        insideMargin = insideMargin,
        onClick = onClick,
        holdDownState = holdDownState,
        enabled = enabled
    )
}