package io.github.proify.lyricon.central.provider

import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import io.github.proify.lyricon.provider.IProviderBinder
import io.github.proify.lyricon.provider.ProviderInfo

class RemoteProvider(private val binder: IProviderBinder) {
    val providerInfo: ProviderInfo = binder.getProviderInfo()
    val service: RemoteProviderService = RemoteProviderService(this)

    private var deathRecipient: IBinder.DeathRecipient? = null

    fun setDeathRecipient(newDeathRecipient: IBinder.DeathRecipient?) {
        deathRecipient?.let { binder.asBinder().unlinkToDeath(it, 0) }

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
        ProviderManager.unregister(this)
    }

    fun onDestroy() {
        service.release()
        setDeathRecipient(null)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is RemoteProvider) return false
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