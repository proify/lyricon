package io.github.proify.lyricon.lyric.bridge.provider

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.BundleCompat
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

/**
 * 歌词提供方的基础信息。
 *
 * @property modulePackageName 提供方模块自身的包名
 * @property musicAppPackageName 目标音乐应用的包名
 * @property logo 提供方的标识图标，可为空
 * @property metadata 扩展元数据，用于承载额外的配置信息
 */
@Parcelize
@TypeParceler<ProviderInfo, ProviderInfo.ParcelerImpl>()
data class ProviderInfo(
    val modulePackageName: String,
    val musicAppPackageName: String,
    val logo: ProviderLogo? = null,
    val metadata: Bundle = Bundle.EMPTY
) : Parcelable {

    object ParcelerImpl : Parceler<ProviderInfo> {

        override fun ProviderInfo.write(parcel: Parcel, flags: Int) {
            val bundle = Bundle().apply {
                putString("modulePackageName", modulePackageName)
                putString("musicAppPackageName", musicAppPackageName)
                putParcelable("logo", logo)
                putBundle("metadata", metadata)
            }
            parcel.writeBundle(bundle)
        }

        override fun create(parcel: Parcel): ProviderInfo {
            val bundle = parcel.readBundle(ProviderInfo::class.java.classLoader) ?: Bundle()
            val modulePackageName = bundle.getString("modulePackageName").orEmpty()
            val musicAppPackageName = bundle.getString("musicAppPackageName").orEmpty()
            val logo = BundleCompat.getParcelable(bundle, "logo", ProviderLogo::class.java)
            val metadata = bundle.getBundle("metadata") ?: Bundle.EMPTY

            return ProviderInfo(
                modulePackageName = modulePackageName,
                musicAppPackageName = musicAppPackageName,
                logo = logo,
                metadata = metadata
            )
        }
    }
}