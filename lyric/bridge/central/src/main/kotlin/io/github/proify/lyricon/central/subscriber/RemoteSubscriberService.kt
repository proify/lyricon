package io.github.proify.lyricon.central.subscriber

import io.github.proify.lyricon.subscriber.IRemoteActivePlayerListener
import io.github.proify.lyricon.subscriber.IRemoteSubscriberService

internal class RemoteSubscriberService(private val subscriber: RemoteSubscriber) :
    IRemoteSubscriberService.Stub() {

    override fun bindActivePlayerListener(listener: IRemoteActivePlayerListener?) =
        subscriber.setRemoteActivePlayerListener(listener)

    override fun disconnect() = SubscriberManager.unregister(subscriber)
}