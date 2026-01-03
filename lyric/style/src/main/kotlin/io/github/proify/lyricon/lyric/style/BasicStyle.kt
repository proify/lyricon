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

package io.github.proify.lyricon.lyric.style

import android.content.SharedPreferences
import android.os.Parcelable
import io.github.proify.android.extensions.jsonx
import io.github.proify.android.extensions.safeDecode
import io.github.proify.android.extensions.toJson
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class BasicStyle(
    var anchor: String = Defaults.ANCHOR,
    var insertionOrder: Int = Defaults.INSERTION_ORDER,
    var width: Float = Defaults.WIDTH,
    var widthInColorOSCapsuleMode: Float = Defaults.WIDTH_IN_COLOROS_CAPSULE_MODE,
    var margins: RectF = Defaults.MARGINS,
    var paddings: RectF = Defaults.PADDINGS,
    var visibilityRules: List<VisibilityRule> = Defaults.VISIBILITY_RULES
) : AbstractStyle(), Parcelable {

    override fun onLoad(preferences: SharedPreferences) {
        anchor =
            preferences.getString("lyric_style_base_anchor", Defaults.ANCHOR) ?: Defaults.ANCHOR
        insertionOrder =
            preferences.getInt("lyric_style_base_insertion_order", Defaults.INSERTION_ORDER)
        width = preferences.getFloat("lyric_style_base_width", Defaults.WIDTH)
        widthInColorOSCapsuleMode = preferences.getFloat(
            "lyric_style_base_width_in_coloros_capsule_mode",
            Defaults.WIDTH_IN_COLOROS_CAPSULE_MODE
        )

        margins = jsonx.safeDecode<RectF>(
            preferences.getString("lyric_style_base_margins", null),
            Defaults.MARGINS
        )
        paddings = jsonx.safeDecode<RectF>(
            preferences.getString("lyric_style_base_paddings", null),
            Defaults.PADDINGS
        )
        visibilityRules = jsonx.safeDecode<MutableList<VisibilityRule>>(
            preferences.getString("lyric_style_base_visibility_rules", null),
            Defaults.VISIBILITY_RULES.toMutableList()
        )
    }

    override fun onWrite(editor: SharedPreferences.Editor) {
        editor.putString("lyric_style_base_anchor", anchor)
        editor.putInt("lyric_style_base_insertion_order", insertionOrder)
        editor.putFloat("lyric_style_base_width", width)
        editor.putFloat("lyric_style_base_width_in_coloros_capsule_mode", widthInColorOSCapsuleMode)
        editor.putString("lyric_style_base_margins", margins.toJson())
        editor.putString("lyric_style_base_paddings", paddings.toJson())
        editor.putString("lyric_style_base_visibility_rules", visibilityRules.toJson())
    }

    object Defaults {
        const val ANCHOR: String = "clock"
        const val INSERTION_ORDER: Int = INSERTION_ORDER_BEFORE
        const val WIDTH: Float = 100f
        const val WIDTH_IN_COLOROS_CAPSULE_MODE: Float = 70f
        val MARGINS: RectF = RectF()
        val PADDINGS: RectF = RectF()
        val VISIBILITY_RULES: List<VisibilityRule> = emptyList()
    }

    companion object {
        const val INSERTION_ORDER_BEFORE: Int = 0
        const val INSERTION_ORDER_AFTER: Int = 1
    }
}