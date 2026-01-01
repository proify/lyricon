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

package io.github.proify.lyricon.app.ui.preference

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.proify.android.extensions.formatToString
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.ui.compose.NumberTextField
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.BasicComponentColors
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.BasicComponentDefaults
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.SuperArrow
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.SuperDialog
import io.github.proify.lyricon.app.util.Utils.commitEdit
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

enum class InputType {
    STRING,
    INTEGER,
    DOUBLE
}

@Composable
fun InputPreference(
    sharedPreferences: SharedPreferences,
    key: String,
    title: String,
    modifier: Modifier = Modifier,
    syncKeys: Array<String> = emptyArray(),
    showKeyboard: Boolean = true,
    inputType: InputType = InputType.STRING,
    minValue: Double = 0.0,
    maxValue: Double = 0.0,
    autoHoldDownState: Boolean = true,
    titleColor: BasicComponentColors = BasicComponentDefaults.titleColor(),
    summary: String? = null,
    summaryColor: BasicComponentColors = BasicComponentDefaults.summaryColor(),
    leftAction: @Composable (() -> Unit)? = null,
    rightActions: @Composable RowScope.() -> Unit = {},
    insideMargin: PaddingValues = top.yukonga.miuix.kmp.basic.BasicComponentDefaults.InsideMargin,
    onClick: (() -> Unit)? = null,
    holdDownState: Boolean = false,
    enabled: Boolean = true
) {
    val prefValueState = rememberStringPreference(sharedPreferences, key, null)
    val currentSummary = summary ?: prefValueState.value ?: stringResource(id = R.string.def)

    var showDialog by remember { mutableStateOf(false) }

    SuperArrow(
        title = title,
        titleColor = titleColor,
        summary = currentSummary,
        summaryColor = summaryColor,
        leftAction = leftAction,
        rightActions = rightActions,
        modifier = modifier,
        insideMargin = insideMargin,
        onClick = {
            onClick?.invoke()
            showDialog = true
        },
        holdDownState = holdDownState || (autoHoldDownState && showDialog),
        enabled = enabled,
    )

    if (showDialog) {
        InputPreferenceDialog(
            title = title,
            initialValue = prefValueState.value ?: "",
            inputType = inputType,
            minValue = minValue,
            maxValue = maxValue,
            showKeyboard = showKeyboard,
            onDismiss = { showDialog = false },
            onSave = { newValue ->
                showDialog = false
                sharedPreferences.commitEdit {
                    if (newValue.isEmpty()) {
                        remove(key)
                        prefValueState.value = null

                        for (syncKey in syncKeys) {
                            remove(syncKey)
                        }
                    } else {
                        putString(key, newValue)

                        for (syncKey in syncKeys) {
                            putString(syncKey, newValue)
                        }
                    }
                }
            }
        )
    }
}

/**
 * 输入弹窗组件
 */
@Composable
private fun InputPreferenceDialog(
    title: String,
    initialValue: String,
    inputType: InputType,
    minValue: Double,
    maxValue: Double,
    showKeyboard: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var inputValue by remember { mutableStateOf(initialValue) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val isNumberInput = inputType != InputType.STRING
    val hasRangeLimit = isNumberInput && maxValue > minValue

    // 验证输入是否有效
    val isValidInput = remember(inputValue, inputType, minValue, maxValue) {
        validateInput(inputValue, inputType, minValue, maxValue)
    }

    // 提示文本
    val hintText = remember(inputValue, hasRangeLimit) {
        if (hasRangeLimit) {
            val currentVal = inputValue.toDoubleOrNull() ?: 0.0
            "${currentVal.formatToString()} (${minValue.formatToString()}-${maxValue.formatToString()})"
        } else run {
            ""
        }
    }

    fun dismiss() {
        onDismiss()
        keyboardController?.hide()
    }

    // 处理键盘和焦点
    LaunchedEffect(Unit) {
        if (showKeyboard) {
            delay(100)
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    SuperDialog(
        title = title,
        show = remember { mutableStateOf(true) },
        onDismissRequest = { dismiss() }
    ) {
        Column(modifier = Modifier.imePadding()) {
            // 根据输入类型选择组件
            when (inputType) {
                InputType.STRING -> {
                    TextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.focusRequester(focusRequester)
                    )
                }

                InputType.INTEGER -> {
                    NumberTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        allowDecimal = false,
                        allowNegative = minValue < 0,
                        modifier = Modifier.focusRequester(focusRequester),
                        autoSelectOnFocus = true,
                        borderColor = if (isValidInput) {
                            MiuixTheme.colorScheme.primary
                        } else {
                            MiuixTheme.colorScheme.error
                        }
                    )
                }

                InputType.DOUBLE -> {
                    NumberTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        allowDecimal = true,
                        allowNegative = minValue < 0,
                        modifier = Modifier.focusRequester(focusRequester),
                        autoSelectOnFocus = true,
                        borderColor = if (isValidInput) {
                            MiuixTheme.colorScheme.primary
                        } else {
                            MiuixTheme.colorScheme.error
                        }
                    )
                }
            }

            // 范围提示
            if (hintText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = hintText,
                        fontSize = 13.sp,
                        color = if (isValidInput) {
                            BasicComponentDefaults.summaryColor().color(true)
                        } else {
                            MiuixTheme.colorScheme.error
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 按钮组
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(
                    text = stringResource(id = R.string.cancel),
                    onClick = { dismiss() },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(20.dp))
                TextButton(
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                    text = stringResource(id = R.string.save),
                    onClick = {
                        val finalValue = formatFinalValue(inputValue, inputType)
                        onSave(finalValue)
                        keyboardController?.hide()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isValidInput
                )
            }
        }
    }
}

// ---------------- Helper Functions ----------------

/**
 * 验证输入值是否有效
 */
private fun validateInput(
    text: String,
    inputType: InputType,
    min: Double,
    max: Double
): Boolean {
    if (text.isEmpty()) return true

    return when (inputType) {
        InputType.STRING -> true
        InputType.INTEGER -> {
            val num = text.toIntOrNull()
            num != null && (max <= min || num in min.toInt()..max.toInt())
        }

        InputType.DOUBLE -> {
            val num = text.toDoubleOrNull()
            num != null && (max <= min || num in min..max)
        }
    }
}

/**
 * 格式化最终保存的值
 */
private fun formatFinalValue(text: String, inputType: InputType): String {
    if (text.isEmpty() || inputType == InputType.STRING) {
        return text
    }

    // 格式化数字，去除多余的0和小数点
    return text.toDoubleOrNull()?.formatToString() ?: text
}