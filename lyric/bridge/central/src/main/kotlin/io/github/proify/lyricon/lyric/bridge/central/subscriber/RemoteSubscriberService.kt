package io.github.proify.lyricon.lyric.bridge.central.subscriber

import io.github.proify.lyricon.lyric.bridge.subscriber.IRemoteActivePlayerListener
import io.github.proify.lyricon.lyric.bridge.subscriber.IRemoteSubscriberService

internal class RemoteSubscriberService(private val subscriber: Subscriber) :
    IRemoteSubscriberService.Stub() {

    override fun bindActivePlayerListener(listener: IRemoteActivePlayerListener?) =
        subscriber.setRemoteActivePlayerListener(listener)

    override fun disconnect() = SubscriberManager.unregister(subscriber)
}