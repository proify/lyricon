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