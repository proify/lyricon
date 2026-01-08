package io.github.proify.lyricon.provider

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 提供者信息
 *
 * @property providerPackageName 提供者包名
 * @property playerPackageName 播放器包名
 * @property logo 播放器Logo
 * @property metadata 元数据
 */
@Serializable
@Parcelize
data class ProviderInfo(
    val providerPackageName: String,
    val playerPackageName: String,
    val logo: ProviderLogo? = null,
    val metadata: ProviderMetadata? = null
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProviderInfo) return false
        return providerPackageName == other.providerPackageName
                && playerPackageName == other.playerPackageName
    }

    override fun hashCode(): Int {
        var result = providerPackageName.hashCode()
        result = 31 * result + playerPackageName.hashCode()
        return result
    }
}