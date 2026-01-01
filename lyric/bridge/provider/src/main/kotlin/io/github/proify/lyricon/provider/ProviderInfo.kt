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

package io.github.proify.lyricon.provider

import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.ParcelCompat
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

@Parcelize
@TypeParceler<ProviderInfo, ProviderInfo.MusicProviderParceler>()
data class ProviderInfo(
    val providerPackageName: String,
    val playerPackageName: String,
    val logo: ProviderLogo? = null,
    val extraMetadata: Map<String, String?>? = null
) : Parcelable {

    object MusicProviderParceler : Parceler<ProviderInfo> {
        private const val PARCEL_VERSION_V1 = 1

        override fun ProviderInfo.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(PARCEL_VERSION_V1)
            parcel.writeString(providerPackageName)
            parcel.writeString(playerPackageName)
            parcel.writeParcelable(logo, flags)
            parcel.writeMetadata(extraMetadata)
        }

        override fun create(parcel: Parcel): ProviderInfo {
            val version = parcel.readInt()
            return when (version) {
                PARCEL_VERSION_V1 -> parcel.readFromV1()
                else -> throw IllegalArgumentException("Unknown parcel version: $version")
            }
        }

        private fun Parcel.readFromV1(): ProviderInfo {
            val providerPackageName = readString().orEmpty()
            val playerPackageName = readString().orEmpty()
            val logo = ParcelCompat.readParcelable(
                this,
                ProviderLogo::class.java.classLoader,
                ProviderLogo::class.java
            )
            val extraMetadata = readMetadata()

            return ProviderInfo(providerPackageName, playerPackageName, logo, extraMetadata)
        }
    }
}