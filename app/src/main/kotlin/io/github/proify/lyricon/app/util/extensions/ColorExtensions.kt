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