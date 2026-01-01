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

package io.github.proify.lyricon.app.ui.compose.color

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable
fun ColorBox(
    modifier: Modifier = Modifier,
    size: DpSize = DpSize(28.dp, 28.dp),
    shape: Shape = CircleShape,
    colors: List<Color?> = emptyList()
) {
    val validColors = remember(colors) {
        colors.filterNotNull().filter { it != Color.Unspecified }
    }

    if (validColors.isEmpty()) return

    Canvas(
        modifier = modifier
            .size(size)
            .clip(shape)
    ) {
        drawCheckerboard()

        val count = validColors.size
        if (count == 1) {
            drawCircle(color = validColors[0])
        } else {
            val sweepAngle = 360f / count
            val startOffset = -90f

            validColors.forEachIndexed { index, itemColor ->
                drawArc(
                    color = itemColor,
                    startAngle = startOffset + (index * sweepAngle),
                    sweepAngle = sweepAngle,
                    useCenter = true
                )
            }
        }
    }
}

private fun DrawScope.drawCheckerboard() {
    val lightGray = Color(0xFFE0E0E0)
    val darkGray = Color(0xFFB0B0B0)
    val checkerSizePx = 4.dp.toPx()

    drawRect(color = lightGray)

    val columns = (size.width / checkerSizePx).toInt() + 1
    val rows = (size.height / checkerSizePx).toInt() + 1

    for (i in 0 until columns) {
        for (j in 0 until rows) {
            if ((i + j) % 2 != 0) {
                drawRect(
                    color = darkGray,
                    topLeft = Offset(i * checkerSizePx, j * checkerSizePx),
                    size = Size(checkerSizePx, checkerSizePx)
                )
            }
        }
    }
}