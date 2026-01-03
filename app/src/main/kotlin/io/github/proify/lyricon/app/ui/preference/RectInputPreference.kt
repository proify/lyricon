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

import android.content.SharedPreferences
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.proify.android.extensions.formatToString
import io.github.proify.android.extensions.fromJsonOrNull
import io.github.proify.android.extensions.toJson
import io.github.proify.lyricon.app.ui.compose.RectFInputDialog
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.BasicComponentColors
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.BasicComponentDefaults
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.SuperArrow
import io.github.proify.lyricon.app.util.Utils.commitEdit
import io.github.proify.lyricon.lyric.style.RectF

@Composable
fun RectInputPreference(
    sharedPreferences: SharedPreferences,
    key: String,
    title: String,
    defaultValue: RectF = RectF(),
    titleColor: BasicComponentColors = BasicComponentDefaults.titleColor(),
    summary: String? = null,
    summaryColor: BasicComponentColors = BasicComponentDefaults.summaryColor(),
    leftAction: @Composable (() -> Unit)? = null,
    rightActions: @Composable RowScope.() -> Unit = {},
    insideMargin: PaddingValues = BasicComponentDefaults.InsideMargin,
    enabled: Boolean = true
) {

    val showDialog = remember { mutableStateOf(false) }
    val prefValueState = rememberStringPreference(sharedPreferences, key, null)

    val value = prefValueState.value
    val rectF = value?.fromJsonOrNull<RectF>() ?: defaultValue

    val currentSummary = summary
        ?: "${rectF.left.formatToString()}, ${rectF.top.formatToString()}, ${rectF.right.formatToString()}, ${rectF.bottom.formatToString()}"

    if (showDialog.value) {
        RectFInputDialog(
            initialLeft = rectF.left,
            initialTop = rectF.top,
            initialRight = rectF.right,
            initialBottom = rectF.bottom,
            show = showDialog,
            title = title,
            onConfirm = { left, top, right, bottom ->
                val rectF = RectF(left, top, right, bottom)
                sharedPreferences.commitEdit {
                    putString(key, rectF.toJson())
                }
            }
        )
    }

    SuperArrow(
        title = title,
        titleColor = titleColor,
        summary = currentSummary,
        summaryColor = summaryColor,
        leftAction = leftAction,
        rightActions = rightActions,
        insideMargin = insideMargin,
        onClick = {
            showDialog.value = true
        },
        holdDownState = showDialog.value,
        enabled = enabled
    )
}