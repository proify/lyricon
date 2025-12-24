package io.github.proify.lyricon.app.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.SuperDialog
import io.github.proify.lyricon.common.extensions.formatToString
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton

/**
 * RectF 输入对话框
 * @param title 对话框标题
 * @param show 控制对话框显示/隐藏的状态
 * @param initialLeft 初始左边距值
 * @param initialTop 初始上边距值
 * @param initialRight 初始右边距值
 * @param initialBottom 初始下边距值
 * @param allowNegative 是否允许负数，默认为 true
 * @param allowDecimal 是否允许小数，默认为 true
 * @param onConfirm 确认回调，返回 left, top, right, bottom 四个值
 * @param onCancel 取消回调
 */
@Composable
fun RectFInputDialog(
    title: String? = null,
    show: MutableState<Boolean>,
    initialLeft: Float = 0f,
    initialTop: Float = 0f,
    initialRight: Float = 0f,
    initialBottom: Float = 0f,
    allowNegative: Boolean = true,
    allowDecimal: Boolean = true,
    onConfirm: (left: Float, top: Float, right: Float, bottom: Float) -> Unit = { _, _, _, _ -> },
    maxValue: Double? = 1000.0,
    minValue: Double? = -1000.0,
    selectAllOnFocus: Boolean = true,

    ) {

    val keyboardController = LocalSoftwareKeyboardController.current
    fun dismiss() {
        keyboardController?.hide()
        if (show.value) show.value = false
    }

    SuperDialog(
        title = title,
        show = show,
        onDismissRequest = {
            dismiss()
        }
    ) {
        // 使用字符串状态来处理输入
        val leftText = remember { mutableStateOf(initialLeft.toDouble().formatToString()) }
        val topText = remember { mutableStateOf(initialTop.toDouble().formatToString()) }
        val rightText = remember { mutableStateOf(initialRight.toDouble().formatToString()) }
        val bottomText = remember { mutableStateOf(initialBottom.toDouble().formatToString()) }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 第一行：Left 和 Top
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NumberTextField(
                    modifier = Modifier
                        .weight(1f),
                    value = leftText.value,
                    onValueChange = { leftText.value = it },
                    label = "左边距",
                    allowNegative = allowNegative,
                    allowDecimal = allowDecimal,
                    maxValue = maxValue,
                    minValue = minValue,
                    autoSelectOnFocus = selectAllOnFocus
                )
                NumberTextField(
                    modifier = Modifier
                        .weight(1f),
                    value = topText.value,
                    onValueChange = { topText.value = it },
                    label = "上边距",
                    allowNegative = allowNegative,
                    allowDecimal = allowDecimal,
                    maxValue = maxValue,
                    minValue = minValue,
                    autoSelectOnFocus = selectAllOnFocus
                )
            }

            // 第二行：Right 和 Bottom
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NumberTextField(
                    modifier = Modifier
                        .weight(1f),
                    value = rightText.value,
                    onValueChange = { rightText.value = it },
                    label = "右边距",
                    allowNegative = allowNegative,
                    allowDecimal = allowDecimal,
                    maxValue = maxValue,
                    minValue = minValue,
                    autoSelectOnFocus = selectAllOnFocus
                )
                NumberTextField(
                    modifier = Modifier
                        .weight(1f),
                    value = bottomText.value,
                    onValueChange = { bottomText.value = it },
                    label = "下边距",
                    allowNegative = allowNegative,
                    allowDecimal = allowDecimal,
                    maxValue = maxValue,
                    minValue = minValue,
                    autoSelectOnFocus = selectAllOnFocus
                )
            }
        }

        // 按钮行
        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                text = stringResource(id = R.string.cancel),
                onClick = {
                    dismiss()
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(20.dp))
            TextButton(
                text = stringResource(id = R.string.confirm),
                onClick = {
                    // 转换为 Float，如果转换失败则使用初始值
                    val left = leftText.value.toFloatOrNull() ?: 0f
                    val top = topText.value.toFloatOrNull() ?: 0f
                    val right = rightText.value.toFloatOrNull() ?: 0f
                    val bottom = bottomText.value.toFloatOrNull() ?: 0f

                    onConfirm(left, top, right, bottom)
                    dismiss()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary()
            )
        }
    }
}