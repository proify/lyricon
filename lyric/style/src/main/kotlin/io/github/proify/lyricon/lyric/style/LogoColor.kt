package io.github.proify.lyricon.lyric.style

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
class LogoColor(
    var mode: Int = MODE_ADAPTIVE,
    var color: Int = 0
) : Parcelable {
    companion object {
        const val MODE_ADAPTIVE: Int = 0
        const val MODE_CUSTOM: Int = 10
        const val MODE_FOLLOW_TEXT: Int = 20
    }
}