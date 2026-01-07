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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.github.proify.android.extensions.fromJson
import io.github.proify.android.extensions.toJson
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.ui.compose.color.ColorBox
import io.github.proify.lyricon.app.ui.compose.color.ColorPaletteDialog
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.Card
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.SuperArrow
import io.github.proify.lyricon.app.util.Utils.commitEdit
import io.github.proify.lyricon.lyric.style.TextColor
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.extra.SuperBottomSheet
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.Delete
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

private const val EMPTY_COLOR = 0
private val ITEM_SPACING = 16.dp

@Composable
fun TextColorPreference(
    sharedPreferences: SharedPreferences,
    key: String,
    title: String,
    defaultColor: Color,
    leftAction: @Composable (() -> Unit)? = null,
) {
    val textColor = rememberLogoColor(sharedPreferences, key)
    val isBottomSheetVisible = remember { mutableStateOf(false) }

    TextColorBottomSheet(
        title = title,
        textColor = textColor,
        defaultColor = defaultColor,
        isVisible = isBottomSheetVisible,
        onReset = { resetTextColor(sharedPreferences, key) },
        onColorChange = { saveTextColor(sharedPreferences, key, textColor) }
    )

    TextColorArrow(
        title = title,
        textColor = textColor,
        leftAction = leftAction,
        onClick = { isBottomSheetVisible.value = true }
    )
}

@Composable
private fun rememberLogoColor(
    sharedPreferences: SharedPreferences,
    key: String
): TextColor {
    val jsonString = rememberStringPreference(sharedPreferences, key, "{}").value
    return remember(jsonString) {
        jsonString?.fromJson<TextColor>() ?: TextColor()
    }
}

private fun resetTextColor(sharedPreferences: SharedPreferences, key: String) {
    sharedPreferences.commitEdit { remove(key) }
}

private fun saveTextColor(
    sharedPreferences: SharedPreferences,
    key: String,
    textColor: TextColor
) {
    sharedPreferences.commitEdit { putString(key, textColor.toJson()) }
}

@Composable
private fun TextColorBottomSheet(
    title: String,
    textColor: TextColor,
    defaultColor: Color,
    isVisible: MutableState<Boolean>,
    onReset: () -> Unit,
    onColorChange: () -> Unit
) {
    SuperBottomSheet(
        show = isVisible,
        title = title,
        rightAction = {
            if (textColor.hasCustomColors()) {
                Row {
                    IconButton(onClick = {
                        isVisible.value = false
                        onReset()
                    }) {
                        Icon(
                            imageVector = MiuixIcons.Useful.Delete,
                            contentDescription = "Reset color"
                        )
                    }
                    Spacer(modifier = Modifier.width(ITEM_SPACING))
                }
            }
        },
        backgroundColor = MiuixTheme.colorScheme.surface,
        insideMargin = DpSize(0.dp, 0.dp),
        onDismissRequest = { isVisible.value = false }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .overScrollVertical()
        ) {
            item("color_settings") {
                ColorSettingsContent(
                    textColor = textColor,
                    defaultColor = defaultColor,
                    onColorChange = onColorChange
                )
            }
        }
    }
}

@Composable
private fun ColorSettingsContent(
    textColor: TextColor,
    defaultColor: Color,
    onColorChange: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = ITEM_SPACING)
            .fillMaxWidth()
    ) {
        ColorPickerItem(
            title = stringResource(R.string.item_text_color_normal),
            initialColor = textColor.normal.toColorOrNull(),
            defaultColor = defaultColor
        ) {
            textColor.normal = it
            onColorChange()
        }
    }

    Spacer(modifier = Modifier.height(ITEM_SPACING))

    Card(
        modifier = Modifier
            .padding(horizontal = ITEM_SPACING)
            .fillMaxWidth()
    ) {
        ColorPickerItem(
            title = stringResource(R.string.item_text_color_background),
            initialColor = textColor.background.toColorOrNull(),
            defaultColor = defaultColor
        ) {
            textColor.background = it
            onColorChange()
        }
        ColorPickerItem(
            title = stringResource(R.string.item_text_color_highlight),
            initialColor = textColor.highlight.toColorOrNull(),
            defaultColor = defaultColor
        ) {
            textColor.highlight = it
            onColorChange()
        }
    }

    Spacer(modifier = Modifier.height(ITEM_SPACING))
}

@Composable
private fun TextColorArrow(
    title: String,
    textColor: TextColor,
    leftAction: @Composable (() -> Unit)?,
    onClick: () -> Unit
) {
    SuperArrow(
        title = title,
        leftAction = leftAction,
        rightActions = {
            ColorPreviewRow(textColor)
        },
        onClick = onClick
    )
}

private fun Int.toColorOrNull(): Color? =
    if (this == 0) null else runCatching { Color(this) }.getOrNull()

@Composable
private fun ColorPreviewRow(textColor: TextColor) {
    val normalColor = textColor.normal.toColorOrNull()
    val backgroundColor = textColor.background.toColorOrNull()
    val highlightColor = textColor.highlight.toColorOrNull()

    normalColor?.let {
        ColorBox(colors = listOf(it))
        Spacer(modifier = Modifier.width(10.dp))
    }

    if (backgroundColor != null || highlightColor != null) {
        ColorBox(colors = listOf(backgroundColor, highlightColor))
        Spacer(modifier = Modifier.width(10.dp))
    }
}

@Composable
private fun ColorPickerItem(
    title: String,
    initialColor: Color?,
    defaultColor: Color,
    leftAction: @Composable (() -> Unit)? = null,
    onColorSelected: (Int) -> Unit
) {
    val isDialogVisible = remember { mutableStateOf(false) }
    val currentColor = remember { mutableStateOf(initialColor) }

    ColorPaletteDialog(
        title = title,
        show = isDialogVisible,
        initialColor = currentColor.value ?: defaultColor,
        onDelete = {
            currentColor.value = null
            onColorSelected(EMPTY_COLOR)
        },
        onConfirm = { color ->
            currentColor.value = color
            onColorSelected(color.toArgb())
        }
    )

    SuperArrow(
        title = title,
        leftAction = leftAction,
        rightActions = {
            currentColor.value?.let {
                ColorBox(colors = listOf(it))
                Spacer(modifier = Modifier.width(10.dp))
            }
        },
        onClick = { isDialogVisible.value = true }
    )
}


private fun TextColor.hasCustomColors(): Boolean =
    normal != EMPTY_COLOR || highlight != EMPTY_COLOR