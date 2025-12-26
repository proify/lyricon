package io.github.proify.lyricon.lyric.model

import android.os.Parcel
import android.os.Parcelable
import io.github.proify.lyricon.lyric.model.DoubleLyricLine.ParcelerImpl.write
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
    var extraMetadata: Map<String, String?>? = null,
    var lyrics: List<DoubleLyricLine>? = null,
) : Parcelable {

    val hasLyrics: Boolean get() = lyrics.isNullOrEmpty().not()

    object ParcelerImpl : Parceler<Song> {
        private const val PARCEL_VERSION_V1 = 1

        override fun Song.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(PARCEL_VERSION_V1)
            parcel.writeString(id)
            parcel.writeString(name)
            parcel.writeString(artist)
            parcel.writeInt(duration)
            parcel.writeMetadata(extraMetadata)

            parcel.writeInt(lyrics?.size ?: -1)
            lyrics?.forEach { it.write(parcel, flags) }
        }

        override fun create(parcel: Parcel): Song {
            val version = parcel.readInt()
            return when (version) {
                PARCEL_VERSION_V1 -> parcel.readFromV1()
                else -> throw IllegalArgumentException("Unsupported version: $version")
            }
        }

        private fun Parcel.readFromV1(): Song {
            val id = readString()
            val name = readString()
            val artist = readString()
            val duration = readInt()
            val extraMetadata = readMetadata()

            val lyricSize = readInt()
            val lyrics = when {
                lyricSize < 0 -> null
                lyricSize == 0 -> emptyList()
                else -> List(lyricSize) {
                    DoubleLyricLine.ParcelerImpl.create(this)
                }
            }

            return Song(
                id = id,
                name = name,
                artist = artist,
                duration = duration,
                extraMetadata = extraMetadata,
                lyrics = lyrics,
            )
        }
    }
}