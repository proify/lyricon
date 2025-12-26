package io.github.proify.lyricon.lyric.style

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class LogoColor(
    var followTextColor: Boolean = true,
    var color: Int = 0
) : Parcelable