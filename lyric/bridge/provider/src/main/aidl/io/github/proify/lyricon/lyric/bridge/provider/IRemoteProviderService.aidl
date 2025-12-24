package io.github.proify.lyricon.lyric.bridge.provider;

import io.github.proify.lyricon.lyric.bridge.provider.IRemotePlayer;

interface IRemoteProviderService {
    IRemotePlayer getPlayer();
    void disconnect();
}