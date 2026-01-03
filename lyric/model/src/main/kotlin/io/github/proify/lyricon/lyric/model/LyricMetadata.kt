package io.github.proify.lyricon.lyric.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@JvmInline
value class LyricMetadata(
    val data: Map<String, String?> = emptyMap(),
) : Map<String, String?> by data,
    Parcelable

fun lyricMetadataOf(vararg pairs: Pair<String, String?>): LyricMetadata =
    LyricMetadata(mapOf(*pairs))
