package io.github.proify.lyricon.lyric.model

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.ParcelCompat
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
@TypeParceler<Song, Song.ParcelerImpl>()
data class Song(
    var id: String? = null,
    var name: String? = null,
    var artist: String? = null,
    var duration: Int = 0,
    var lyrics: List<DoubleLyricLine> = mutableListOf(),
    var metadata: Map<String, String> = mutableMapOf()
) : Parcelable {

    val hasLyrics: Boolean get() = lyrics.isNotEmpty()

    constructor(source: Song) : this(
        id = source.id,
        name = source.name,
        artist = source.artist,
        duration = source.duration,
        lyrics = source.lyrics.map { it.deepCopy() },
        metadata = source.metadata.toMap()
    )

    fun copy(): Song = Song(this)

    object ParcelerImpl : Parceler<Song> {

        private const val PARCEL_VERSION_V1 = 1
        private const val PARCEL_VERSION = PARCEL_VERSION_V1

        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_ARTIST = "artist"
        private const val KEY_DURATION = "duration"
        private const val KEY_METADATA = "metadata"

        override fun Song.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(PARCEL_VERSION)
            parcel.writeParcelableList(lyrics, flags)
            parcel.writeBundle(Bundle().apply {
                putString(KEY_ID, id)
                putString(KEY_NAME, name)
                putString(KEY_ARTIST, artist)
                putInt(KEY_DURATION, duration)
                putBundle(KEY_METADATA, Bundle().apply {
                    metadata.forEach { (k, v) -> putString(k, v) }
                })
            })
        }

        override fun create(parcel: Parcel): Song {
            val version = parcel.readInt()
            when (version) {
                PARCEL_VERSION_V1 -> {
                    val lyrics =
                        ParcelCompat.readParcelableList(
                            parcel,
                            mutableListOf<DoubleLyricLine>(),
                            DoubleLyricLine::class.java.classLoader,
                            DoubleLyricLine::class.java
                        )

                    val bundle = parcel.readBundle(Song::class.java.classLoader) ?: Bundle.EMPTY
                    val id = bundle.getString(KEY_ID)
                    val name = bundle.getString(KEY_NAME)
                    val artist = bundle.getString(KEY_ARTIST)
                    val duration = bundle.getInt(KEY_DURATION)

                    val metadata = bundle.getBundle(KEY_METADATA)?.let { b ->
                        b.keySet().associateWith { b.getString(it).orEmpty() }
                    } ?: emptyMap()

                    return Song(id, name, artist, duration, lyrics, metadata)
                }

                else -> throw IllegalArgumentException("Unknown parcel version: $version")
            }
        }
    }
}