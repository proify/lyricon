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

package io.github.proify.lyricon.app.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun NumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    allowDecimal: Boolean = true,
    allowNegative: Boolean = true,
    maxValue: Double? = null,
    minValue: Double? = null,
    autoSelectOnFocus: Boolean = false,
    borderColor: Color = MiuixTheme.colorScheme.primary,
) {
    // 使用 TextFieldValue 来控制选择
    var textFieldValueState by remember {
        mutableStateOf(TextFieldValue(text = value))
    }

    // 跟踪焦点状态
    var isFocused by remember { mutableStateOf(false) }
    var shouldSelectAll by remember { mutableStateOf(false) }

    // 当 value 从外部改变时更新
    LaunchedEffect(value) {
        if (textFieldValueState.text != value && autoSelectOnFocus) {
            textFieldValueState = TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }

    // 当需要全选时执行
    LaunchedEffect(shouldSelectAll) {
        if (shouldSelectAll && textFieldValueState.text.isNotEmpty()) {
            textFieldValueState = textFieldValueState.copy(
                selection = TextRange(0, textFieldValueState.text.length)
            )
            shouldSelectAll = false
        }
    }

    Column(modifier = modifier) {
        TextField(
            borderColor = borderColor,
            label = label,
            value = textFieldValueState,
            onValueChange = { newValue ->
                // 过滤输入，只允许数字相关字符
                val filtered = filterNumericInput(
                    input = newValue.text,
                    allowDecimal = allowDecimal,
                    allowNegative = allowNegative
                )

                // 验证数值范围
                val isValid = validateRange(filtered, minValue, maxValue)

                if (isValid || filtered.isEmpty() || filtered == "-" || filtered.endsWith(".")) {
                    textFieldValueState = newValue.copy(text = filtered)
                    onValueChange(filtered)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused && !isFocused) {
                        // 刚获得焦点
                        shouldSelectAll = true
                    }
                    isFocused = focusState.isFocused
                },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (allowDecimal) {
                    KeyboardType.Decimal
                } else {
                    KeyboardType.Number
                }
            ),
            singleLine = true
        )
    }
}

/**
 * 过滤输入，只保留有效的数字字符
 */
private fun filterNumericInput(
    input: String,
    allowDecimal: Boolean,
    allowNegative: Boolean
): String {
    if (input.isEmpty()) return input

    var result = input

    // 只允许数字、小数点和负号
    result = result.filter { char ->
        char.isDigit() ||
                (char == '.' && allowDecimal) ||
                (char == '-' && allowNegative)
    }

    // 负号只能在开头
    if (allowNegative) {
        val negativeCount = result.count { it == '-' }
        if (negativeCount > 0) {
            val hasLeadingNegative = result.startsWith('-')
            result = result.replace("-", "")
            if (hasLeadingNegative) {
                result = "-$result"
            }
        }
    }

    // 只能有一个小数点
    if (allowDecimal) {
        val dotCount = result.count { it == '.' }
        if (dotCount > 1) {
            val firstDotIndex = result.indexOf('.')
            result = result.take(firstDotIndex + 1) +
                    result.substring(firstDotIndex + 1).replace(".", "")
        }
    }

    return result
}

/**
 * 验证数值范围
 */
private fun validateRange(
    value: String,
    minValue: Double?,
    maxValue: Double?
): Boolean {
    // 允许中间状态：空字符串、单独的负号、以小数点结尾
    if (value.isEmpty() || value == "-" || value.endsWith(".") || value == "-.") {
        return true
    }

    val numValue = value.toDoubleOrNull() ?: return false

    if (minValue != null && numValue < minValue) {
        return false
    }

    if (maxValue != null && numValue > maxValue) {
        return false
    }

    return true
}