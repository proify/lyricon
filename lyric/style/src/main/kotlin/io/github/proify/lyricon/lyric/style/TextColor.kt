package io.github.proify.lyricon.lyric.style

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class TextColor(
    var normal: Int = 0,
    var background: Int = 0,
    var highlight: Int = 0,
) : Parcelable