package io.github.proify.lyricon.subscriber

import io.github.proify.lyricon.subscriber.service.SubscriberServiceProxy

internal class RemoteSubscriberBinder(
    private val subscriber: LyricSubscriber,
    private val serviceProxy: SubscriberServiceProxy
) : IRemoteSubscriberBinder.Stub() {

    override fun onRegistrationCallback(service: IRemoteSubscriberService?) =
        serviceProxy.bindService(service)

    override fun getSubscriberInfo(): SubscriberInfo = subscriber.subscriberInfo
}