package io.github.proify.lyricon.subscriber.service

import io.github.proify.lyricon.subscriber.LyricSubscriber

interface ConnectionListener {
    fun onConnected(subscriber: LyricSubscriber?)
    fun onDisconnected(subscriber: LyricSubscriber?)
}