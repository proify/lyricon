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

package io.github.proify.lyricon.subscriber

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

/**
 * 歌词订阅者信息。
 *
 * @property packageName 订阅者包名
 * @property processName 订阅者进程名
 */
@Parcelize
@TypeParceler<SubscriberInfo, SubscriberInfo.ParcelerImpl>()
data class SubscriberInfo(
    val packageName: String,
    val processName: String
) : Parcelable {

    companion object ParcelerImpl : Parceler<SubscriberInfo> {

        override fun SubscriberInfo.write(parcel: Parcel, flags: Int) {
            parcel.writeBundle(Bundle().apply {
                putString("packageName", packageName)
                putString("processName", processName)
            })
        }

        override fun create(parcel: Parcel): SubscriberInfo {
            val bundle = parcel.readBundle(SubscriberInfo::class.java.classLoader)
            val packageName = bundle?.getString("packageName").orEmpty()
            val processName = bundle?.getString("processName").orEmpty()

            return SubscriberInfo(packageName, processName)
        }
    }
}