package io.github.proify.lyricon.lyric.subscriber.service

import io.github.proify.lyricon.lyric.bridge.subscriber.IRemoteSubscriberService

internal interface SubscriberServiceProxy : SubscriberService {
    fun bindService(service: IRemoteSubscriberService?)
}