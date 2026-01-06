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

package io.github.proify.lyricon.lyric.view.line

import android.graphics.Paint
import io.github.proify.lyricon.lyric.model.interfaces.ILyricTiming

data class WordModel(
    override var begin: Long,
    override var end: Long,
    override var duration: Long,
    val text: String,
) : ILyricTiming {

    var previous: WordModel? = null
    var next: WordModel? = null

    var textWidth: Float = 0f
        private set

    var startPosition = 0f
        private set

    var endPosition: Float = 0f
        private set

    val chars: CharArray = text.toCharArray()
    val charWidths = FloatArray(text.length)
    val charStartPositions = FloatArray(text.length) { 0f }
    val charEndPositions = FloatArray(text.length) { 0f }

    val charOffsetMode = chars.all { isCharChinese(it) }
    val charOffsetYArray = Array(chars.size) { CharOffset() }

    private fun isCharChinese(char: Char) = char in '\u4e00'..'\u9fff'

    inner class CharOffset(
        var from: Float = 0f,
        var to: Float = 0f,
        var value: Float = 0f
    )

    fun getCharOffsetY(charIndex: Int) = charOffsetYArray.getOrNull(charIndex)?.value ?: 0f

    val offsetY: Float
        get() = charOffsetYArray.firstOrNull()?.from ?: 0f

    fun updateSizes(previous: WordModel?, paint: Paint) {
        paint.getTextWidths(chars, 0, chars.size, charWidths)
        textWidth = charWidths.sum()

        val dropDistance = paint.textSize * 0.07f
        charOffsetYArray.forEach { offset ->
            //不允许修改，避免动画重置...😅
            if (offset.from != 0f) {
                return@forEach
            }

            offset.from = dropDistance
            offset.to = 0f
            offset.value = dropDistance
        }

        startPosition = previous?.endPosition ?: 0f
        endPosition = startPosition + textWidth


        var currentPosition = startPosition
        for (i in chars.indices) {
            charStartPositions[i] = currentPosition
            currentPosition += charWidths[i]
            charEndPositions[i] = currentPosition
        }
    }
}