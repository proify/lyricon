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
import io.github.proify.android.extensions.formatToString
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.SuperDialog
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton

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
        onDismissRequest = { dismiss() }
    ) {

        val leftText = remember { mutableStateOf(initialLeft.toDouble().formatToString()) }
        val topText = remember { mutableStateOf(initialTop.toDouble().formatToString()) }
        val rightText = remember { mutableStateOf(initialRight.toDouble().formatToString()) }
        val bottomText = remember { mutableStateOf(initialBottom.toDouble().formatToString()) }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NumberTextField(
                    modifier = Modifier.weight(1f),
                    value = leftText.value,
                    onValueChange = { leftText.value = it },
                    label = stringResource(R.string.rectf_left),
                    allowNegative = allowNegative,
                    allowDecimal = allowDecimal,
                    maxValue = maxValue,
                    minValue = minValue,
                    autoSelectOnFocus = selectAllOnFocus
                )

                NumberTextField(
                    modifier = Modifier.weight(1f),
                    value = topText.value,
                    onValueChange = { topText.value = it },
                    label = stringResource(R.string.rectf_top),
                    allowNegative = allowNegative,
                    allowDecimal = allowDecimal,
                    maxValue = maxValue,
                    minValue = minValue,
                    autoSelectOnFocus = selectAllOnFocus
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NumberTextField(
                    modifier = Modifier.weight(1f),
                    value = rightText.value,
                    onValueChange = { rightText.value = it },
                    label = stringResource(R.string.rectf_right),
                    allowNegative = allowNegative,
                    allowDecimal = allowDecimal,
                    maxValue = maxValue,
                    minValue = minValue,
                    autoSelectOnFocus = selectAllOnFocus
                )

                NumberTextField(
                    modifier = Modifier.weight(1f),
                    value = bottomText.value,
                    onValueChange = { bottomText.value = it },
                    label = stringResource(R.string.rectf_bottom),
                    allowNegative = allowNegative,
                    allowDecimal = allowDecimal,
                    maxValue = maxValue,
                    minValue = minValue,
                    autoSelectOnFocus = selectAllOnFocus
                )
            }
        }

        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                text = stringResource(R.string.cancel),
                onClick = { dismiss() },
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(20.dp))

            TextButton(
                text = stringResource(R.string.confirm),
                onClick = {
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