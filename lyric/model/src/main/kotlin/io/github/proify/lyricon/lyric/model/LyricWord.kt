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
import androidx.annotation.IntRange
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.Serializable

/**
 * @property begin 开始时间
 * @property end 结束时间
 * @property duration 持续时间
 * @property text 内容
 */
@Serializable
@Parcelize
@TypeParceler<LyricWord, LyricWord.ParcelerImpl>()
data class LyricWord(
    @param:IntRange(0) override var begin: Int = 0,
    @param:IntRange(0) override var end: Int = 0,
    @param:IntRange(0) override var duration: Int = 0,
    var text: String? = null,
    var extraMetadata: Map<String, String?>? = null
) : LyricTiming, Parcelable {

    object ParcelerImpl : Parceler<LyricWord> {
        private const val PARCEL_VERSION_V1 = 1

        override fun LyricWord.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(PARCEL_VERSION_V1)
            parcel.writeInt(begin)
            parcel.writeInt(end)
            parcel.writeInt(duration)
            parcel.writeString(text)
            parcel.writeMetadata(extraMetadata)
        }

        override fun create(parcel: Parcel): LyricWord {
            val version = parcel.readInt()
            return when (version) {
                PARCEL_VERSION_V1 -> parcel.readFromV1()
                else -> throw IllegalArgumentException("Unknown version $version")
            }
        }

        private fun Parcel.readFromV1(): LyricWord {
            val begin = readInt()
            val end = readInt()
            val duration = readInt()
            val text = readString()
            val extraMetadata = readMetadata()

            return LyricWord(
                begin = begin,
                end = end,
                duration = duration,
                text = text,
                extraMetadata = extraMetadata
            )
        }
    }
}