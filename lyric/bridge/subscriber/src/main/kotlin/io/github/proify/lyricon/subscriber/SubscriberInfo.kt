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