package io.github.proify.lyricon.lyric.subscriber

import io.github.proify.lyricon.lyric.bridge.subscriber.IRemoteSubscriberBinder
import io.github.proify.lyricon.lyric.bridge.subscriber.IRemoteSubscriberService
import io.github.proify.lyricon.lyric.bridge.subscriber.SubscriberInfo
import io.github.proify.lyricon.lyric.subscriber.service.SubscriberServiceProxy

internal class RemoteSubscriberBinder(
    private val subscriber: Subscriber,
    private val serviceProxy: SubscriberServiceProxy
) : IRemoteSubscriberBinder.Stub() {

    override fun onRegistrationCallback(service: IRemoteSubscriberService?) =
        serviceProxy.bindService(service)

    override fun getSubscriberInfo(): SubscriberInfo = subscriber.subscriberInfo
}