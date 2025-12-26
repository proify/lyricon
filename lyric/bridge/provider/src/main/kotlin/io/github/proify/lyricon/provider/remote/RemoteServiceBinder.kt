package io.github.proify.lyricon.provider.remote

internal interface RemoteServiceBinder<T> {
    fun bindRemoteService(service: T)
}