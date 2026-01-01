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

package io.github.proify.lyricon.lyric.view

import android.graphics.Color
import android.graphics.Typeface

open class LyricLineConfig(
    var text: TextConfig = MainTextConfig(),
    var marquee: MarqueeConfig = MainMarqueeConfig(),
    var syllable: SyllableConfig = MainSyllableConfig()
)

data class DoubleLyricConfig(
    var primary: MainTextConfig = MainTextConfig(),
    var secondary: SecondaryTextConfig = SecondaryTextConfig(),

    var marquee: MainMarqueeConfig = MainMarqueeConfig(),
    var syllable: MainSyllableConfig = MainSyllableConfig(),
    // var secondaryMarquee: MarqueeConfig = SecondaryMarqueeConfig(),
    //var secondarySyllable: SyllableConfig = SecondarySyllableConfig()
)

interface TextConfig {
    var textColor: Int
    var textSize: Float
    var typeface: Typeface
}

interface MarqueeConfig {
    var ghostSpacing: Float
    var scrollSpeed: Float
    var initialDelay: Int
    var loopDelay: Int
    var repeatCount: Int
    var stopAtEnd: Boolean
}

open class DefaultMarqueeConfig(
    override var scrollSpeed: Float = 40f,
    override var ghostSpacing: Float = 70f.dp,
    override var initialDelay: Int = 300,
    override var loopDelay: Int = 700,
    override var repeatCount: Int = -1,
    override var stopAtEnd: Boolean = false,
) : MarqueeConfig

interface SyllableConfig {
    var backgroundColor: Int
    var highlightColor: Int
}

////////////////////////////////////////////////////////////////////////////////////

data class MainTextConfig(
    override var textColor: Int = Color.BLACK,
    override var textSize: Float = 16f.sp,
    override var typeface: Typeface = Typeface.DEFAULT,
) : TextConfig

open class MainMarqueeConfig : DefaultMarqueeConfig()

class MainSyllableConfig(
    override var highlightColor: Int = Color.BLACK,
    override var backgroundColor: Int = Color.GRAY,
) : SyllableConfig

////////////////////////////////////////////////////////////////////////////////////
//
//class SecondaryMarqueeConfig : DefaultMarqueeConfig()
//
//class SecondarySyllableConfig(
//    override var highlightColor: Int = Color.BLACK,
//    override var backgroundColor: Int = Color.GRAY,
//) : SyllableConfig

class SecondaryTextConfig(
    override var textColor: Int = Color.GRAY,
    override var textSize: Float = 14f.sp,
    override var typeface: Typeface = Typeface.DEFAULT
) : TextConfig