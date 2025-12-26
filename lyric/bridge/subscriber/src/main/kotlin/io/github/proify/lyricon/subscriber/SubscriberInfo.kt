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