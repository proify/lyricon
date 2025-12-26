package io.github.proify.lyricon.subscriber.service

interface SubscriberService {
    fun registerActivePlayerListener(listener: OnActivePlayerListener)
    fun unregisterActivePlayerListener(listener: OnActivePlayerListener)
    val isActivate: Boolean
    var connectionListener: ConnectionListener?
    fun disconnect()
}