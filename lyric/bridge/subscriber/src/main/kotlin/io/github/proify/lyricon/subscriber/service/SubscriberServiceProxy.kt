package io.github.proify.lyricon.subscriber.service

import io.github.proify.lyricon.subscriber.IRemoteSubscriberService

internal interface SubscriberServiceProxy : SubscriberService {
    fun bindService(service: IRemoteSubscriberService?)
}