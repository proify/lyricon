/*
 * Lyricon – An Xposed module that extends system functionality
 * Copyright (C) 2026 Proify
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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