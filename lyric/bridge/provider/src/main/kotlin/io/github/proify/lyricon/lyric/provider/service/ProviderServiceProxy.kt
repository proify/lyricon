package io.github.proify.lyricon.lyric.provider.service

import io.github.proify.lyricon.lyric.bridge.provider.IRemoteProviderService

internal interface ProviderServiceProxy : ProviderService {
    fun bindService(service: IRemoteProviderService?)
}