@file:Suppress("unused")

package io.github.proify.lyricon.lyric.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class LyricMetadata(
    private val map: Map<String, String?> = emptyMap(),
) : Map<String, String?> by map, Parcelable

fun lyricMetadataOf(vararg pairs: Pair<String, String?>): LyricMetadata =
    LyricMetadata(mapOf(*pairs))