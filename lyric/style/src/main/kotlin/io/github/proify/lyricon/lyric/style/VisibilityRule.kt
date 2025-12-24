package io.github.proify.lyricon.lyric.style

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class VisibilityRule(
    val id: String,
    var mode: Int
) : Parcelable {
    companion object {
        const val MODE_NORMAL = 0
        const val MODE_ALWAYS_VISIBLE = 1
        const val MODE_ALWAYS_HIDDEN = 2
        const val MODE_HIDE_WHEN_PLAYING = 3
    }
}