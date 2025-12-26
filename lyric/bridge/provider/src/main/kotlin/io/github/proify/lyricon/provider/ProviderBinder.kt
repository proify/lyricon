package io.github.proify.lyricon.provider

import android.util.Log
import io.github.proify.lyricon.provider.remote.RemoteServiceBinder
import java.util.concurrent.CopyOnWriteArraySet

internal class ProviderBinder(
    private val provider: LyriconProvider,
    private val providerService: ProviderService,
    private val remoteServiceBinder: RemoteServiceBinder<IRemoteService?>
) : IProviderBinder.Stub() {

    private val registrationCallbacks = CopyOnWriteArraySet<RegistrationCallback>()

    fun addRegistrationCallback(callback: RegistrationCallback) {
        registrationCallbacks.add(callback)
    }

    fun removeRegistrationCallback(callback: RegistrationCallback) {
        registrationCallbacks.remove(callback)
    }

    fun interface RegistrationCallback {
        fun onRegistered()
    }

    override fun onRegistrationCallback(remoteProviderService: IRemoteService?) {
        remoteServiceBinder.bindRemoteService(remoteProviderService)
        notifyRegistrationCallbacks()
    }

    override fun getProviderService(): IProviderService = providerService

    override fun getProviderInfo(): ProviderInfo = provider.providerInfo

    private fun notifyRegistrationCallbacks() {
        registrationCallbacks.forEach {
            try {
                it.onRegistered()
            } catch (e: Exception) {
                Log.e(TAG, "Error in callback", e)
            }
        }
    }

    companion object {
        private const val TAG = "RemoteProviderBinder"
    }
}