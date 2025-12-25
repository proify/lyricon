package io.github.proify.lyricon.app.ui.preference

import android.content.ClipData
import android.content.SharedPreferences
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import io.github.proify.lyricon.app.R
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.Card
import io.github.proify.lyricon.app.ui.compose.custom.miuix.basic.ColorPalette
import io.github.proify.lyricon.app.ui.compose.custom.miuix.extra.SuperArrow
import io.github.proify.lyricon.common.extensions.fromJson
import io.github.proify.lyricon.common.extensions.toJson
import io.github.proify.lyricon.lyric.style.TextColor
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.SuperBottomSheet
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.Delete
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun TextColorPreference(
    sharedPreferences: SharedPreferences,
    key: String,
    title: String,
    defaultColor: Color,
    leftAction: @Composable (() -> Unit)? = null,
) {
    val textColor =
        rememberStringPreference(sharedPreferences, key, "{}").value?.fromJson<TextColor>()
            ?: TextColor()

    val showColorSheet = remember { mutableStateOf(false) }
    fun getColor(color: Int): Color? = if (color != 0) Color(color) else null

    SuperBottomSheet(
        show = showColorSheet,
        title = title,
        rightAction = {
            if (textColor.normal != 0 || textColor.highlight != 0) {
                Row {
                    IconButton(onClick = {
                        showColorSheet.value = false
                        sharedPreferences.edit { remove(key) }
                    }) {
                        Icon(
                            imageVector = MiuixIcons.Useful.Delete,
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
        },
        backgroundColor = MiuixTheme.colorScheme.surface,
        insideMargin = DpSize(0.dp, 0.dp),
        onDismissRequest = { showColorSheet.value = false }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .overScrollVertical()
        ) {
            item("colors") {

                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    ColorPickerItem(
                        title = stringResource(R.string.item_text_color_normal),
                        initialColor = getColor(textColor.normal),
                        defaultColor = defaultColor
                    ) {
                        textColor.normal = it
                        sharedPreferences.edit { putString(key, textColor.toJson()) }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    ColorPickerItem(
                        title = stringResource(R.string.item_text_color_background),
                        initialColor = getColor(textColor.highlight),
                        defaultColor = defaultColor
                    ) {
                        textColor.background = it
                        sharedPreferences.edit { putString(key, textColor.toJson()) }
                    }
                    ColorPickerItem(
                        title = stringResource(R.string.item_text_color_highlight),
                        initialColor = getColor(textColor.highlight),
                        defaultColor = defaultColor
                    ) {
                        textColor.highlight = it
                        sharedPreferences.edit { putString(key, textColor.toJson()) }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

    }

    SuperArrow(
        title = title,
        leftAction = leftAction,
        rightActions = {
            val color1 = getColor(textColor.normal)
            val color2 = getColor(textColor.highlight)
            if (color1 != null && color2 != null) {
                DualCircleColorBox(color1, color2)
            } else if (color1 != null || color2 != null) {
                (color1 ?: color2)?.let { CircleColorBox(it) }
            }
            Spacer(modifier = Modifier.width(10.dp))
        },
        onClick = { showColorSheet.value = true }
    )
}

@Composable
private fun ColorPickerItem(
    title: String,
    initialColor: Color?,
    defaultColor: Color,
    leftAction: @Composable (() -> Unit)? = null,
    onColorSelected: (Int) -> Unit
) {
    val showDialog = remember { mutableStateOf(false) }
    val mcolor = remember { mutableStateOf(initialColor) }

    ColorPaletteDialog(
        title = title,
        showBottomSheet = showDialog,
        initialColor = mcolor.value ?: defaultColor,
        onDelect = {
            mcolor.value = null
            onColorSelected(0)
        }
    ) { color ->
        mcolor.value = color
        onColorSelected(color.toArgb())
    }
    SuperArrow(
        title = title,
        leftAction = leftAction,
        rightActions = {
            mcolor.value?.let {
                CircleColorBox(it)
                Spacer(modifier = Modifier.width(10.dp))
            }
        },
        onClick = { showDialog.value = true }
    )
}

@Composable
private fun ColorPaletteDialog(
    title: String,
    showBottomSheet: MutableState<Boolean>,
    initialColor: Color = Color.Red,
    onDelect: (() -> Unit),
    onColorConfirm: (Color) -> Unit,

    ) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var selectedColor by remember { mutableStateOf(initialColor) }
    var customHex by remember { mutableStateOf(argbToHex(selectedColor)) }
    val clipboard = LocalClipboard.current
    val hapticFeedback = LocalHapticFeedback.current

    fun dismiss() {
        showBottomSheet.value = false
        keyboardController?.hide()
    }

    SuperBottomSheet(
        show = showBottomSheet,
        title = title,
        rightAction = {
            IconButton(onClick = {
                onDelect()
                dismiss()
            }) {
                Icon(
                    imageVector = MiuixIcons.Useful.Delete,
                    contentDescription = null
                )
            }
        },
        onDismissRequest = { dismiss() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .overScrollVertical()
        ) {
            item("color_palette") {
                ColorPalette(
                    initialColor = selectedColor,
                    onColorChanged = {
                        selectedColor = it
                        customHex = argbToHex(it)
                    }
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    TextField(
                        modifier = Modifier.weight(1f),
                        value = customHex,
                        onValueChange = {
                            customHex = it
                            runCatching { selectedColor = Color(toArgb(it)) }
                        },
                        label = stringResource(id = R.string.hint_custom_color),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(
                        backgroundColor = MiuixTheme.colorScheme.surface,
                        onClick = {
                            clipboard.nativeClipboard.setPrimaryClip(
                                ClipData.newPlainText(
                                    "color",
                                    customHex
                                )
                            )
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.content_copy_24px),
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(
                        backgroundColor = MiuixTheme.colorScheme.surface,
                        onClick = {
                            runCatching {
                                val clipData = clipboard.nativeClipboard.primaryClip
                                if (clipData != null && clipData.itemCount > 0) {
                                    customHex = clipData.getItemAt(0)?.text.toString()
                                    selectedColor = Color(toArgb(customHex))
                                }
                            }
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.content_paste_24px),
                            contentDescription = null
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(
                        text = stringResource(R.string.cancel),
                        onClick = { dismiss() },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(16.dp))
                    TextButton(
                        text = stringResource(R.string.confirm),
                        onClick = {
                            onColorConfirm(selectedColor)
                            dismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

private fun toArgb(colorHex: String): Long {
    var hex = colorHex.removePrefix("#")
    if (hex.length == 6) hex = "FF$hex"
    require(hex.length == 8) { "Invalid color format: $colorHex" }
    return hex.toULong(16).toLong()
}

private fun argbToHex(color: Color): String = String.format("#%08X", color.toArgb())

@Composable
private fun DualCircleColorBox(
    color1: Color,
    color2: Color,
) {
    val light = Color(0xFFE0E0E0)
    val dark = Color(0xFFB0B0B0)
    val checkerSize = 4.dp

    Canvas(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
    ) {
        val pxSize = checkerSize.toPx()
        val columns = (size.width / pxSize).toInt() + 1
        val rows = (size.height / pxSize).toInt() + 1
        val radius = size.minDimension / 2
        val center = size.center

        // 绘制棋盘背景
        for (i in 0 until columns) {
            for (j in 0 until rows) {
                drawRect(
                    color = if ((i + j) % 2 == 0) light else dark,
                    topLeft = Offset(i * pxSize, j * pxSize),
                    size = Size(pxSize, pxSize)
                )
            }
        }

        // 绘制左半圆
        drawArc(
            color = color1,
            startAngle = 90f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )

        // 绘制右半圆
        drawArc(
            color = color2,
            startAngle = 270f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
    }
}


@Composable
private fun CircleColorBox(
    color: Color,
) {
    val light = Color(0xFFE0E0E0)
    val dark = Color(0xFFB0B0B0)
    val checkerSize = 4.dp

    Canvas(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
    ) {
        val pxSize = checkerSize.toPx()
        val columns = (size.width / pxSize).toInt() + 1
        val rows = (size.height / pxSize).toInt() + 1
        val halfWidth = size.width / 2
        val halfHeight = size.height / 2
        val radius = size.minDimension / 2

        for (i in 0 until columns) {
            for (j in 0 until rows) {
                drawRect(
                    color = if ((i + j) % 2 == 0) light else dark,
                    topLeft = Offset(i * pxSize, j * pxSize),
                    size = Size(pxSize, pxSize)
                )
            }
        }

        drawCircle(
            color = color,
            radius = radius,
            center = Offset(halfWidth, halfHeight)
        )
    }
}