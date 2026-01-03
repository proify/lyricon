package io.github.proify.lyricon.provider

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@JvmInline
value class ProviderMetadata(
    val map: Map<String, String?> = emptyMap(),
) : Map<String, String?> by map, Parcelable

fun providerMetadataOf(vararg pairs: Pair<String, String?>) =
    ProviderMetadata(mapOf(*pairs))