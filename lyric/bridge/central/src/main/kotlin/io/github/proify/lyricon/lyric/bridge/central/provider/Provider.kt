package io.github.proify.lyricon.lyric.bridge.central.provider

import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import io.github.proify.lyricon.lyric.bridge.provider.IRemoteProviderBinder
import io.github.proify.lyricon.lyric.bridge.provider.ProviderInfo

class Provider(private val binder: IRemoteProviderBinder) {
    val providerInfo: ProviderInfo = binder.getProviderInfo()
    val service: RemoteProviderService = RemoteProviderService(this)

    private var deathRecipient: IBinder.DeathRecipient? = null

    @Synchronized
    fun setDeathRecipient(newDeathRecipient: IBinder.DeathRecipient?) {
        if (deathRecipient != null) {
            binder.asBinder().unlinkToDeath(deathRecipient!!, 0)
        }
        if (newDeathRecipient != null) {
            try {
                binder.asBinder().linkToDeath(newDeathRecipient, 0)
            } catch (e: RemoteException) {
                Log.e(TAG, "link to Death failed", e)
            }
        }
        this.deathRecipient = newDeathRecipient
    }

    fun destroy() {
        setDeathRecipient(null)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Provider) return false
        return this.providerInfo == other.providerInfo
    }

    override fun hashCode(): Int {
        return providerInfo.hashCode()
    }

    override fun toString(): String {
        return "RemoteProvider{" + this.providerInfo + "}"
    }

    companion object {
        private const val TAG = "Provider"
    }
}