package io.github.proify.lyricon.lyric.style

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
class LyricStyle(
    var basicStyle: BasicStyle = BasicStyle(),
    var packageStyle: PackageStyle = PackageStyle()
) : Parcelable