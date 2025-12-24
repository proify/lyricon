package io.github.proify.lyricon.lyric.subscriber.service

import io.github.proify.lyricon.lyric.subscriber.Subscriber

interface ConnectionListener {
    fun onConnected(subscriber: Subscriber?)
    fun onDisconnected(subscriber: Subscriber?)
}