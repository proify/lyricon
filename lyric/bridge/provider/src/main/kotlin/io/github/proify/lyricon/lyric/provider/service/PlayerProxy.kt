package io.github.proify.lyricon.lyric.provider.service

import io.github.proify.lyricon.lyric.bridge.provider.IRemotePlayer

internal interface PlayerProxy : Player {
    fun bindPlayer(player: IRemotePlayer?)
}