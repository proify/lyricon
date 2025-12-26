package io.github.proify.lyricon.provider.remote

import io.github.proify.lyricon.provider.LyriconProvider

interface ConnectionListener {
    fun onConnected(provider: LyriconProvider) {}
    fun onReconnected(provider: LyriconProvider) {}
    fun onDisconnected(provider: LyriconProvider) {}
    fun onConnectTimeout(provider: LyriconProvider) {}
}