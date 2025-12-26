package io.github.proify.lyricon.app.util.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

fun Color.toHexString(): String =
    String.format("#%08X", toArgb())

fun String.parseHexColor(): Color {
    var hex = removePrefix("#")
    if (hex.length == 6) hex = "FF$hex"
    require(hex.length == 8) { "Invalid color format: $this" }
    return Color(hex.toULong(16).toLong())
}