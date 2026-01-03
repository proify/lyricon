/*
 * Copyright 2026 Proify
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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