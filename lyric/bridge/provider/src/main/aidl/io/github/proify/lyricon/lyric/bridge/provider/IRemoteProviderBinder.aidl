package io.github.proify.lyricon.lyric.bridge.provider;

import io.github.proify.lyricon.lyric.bridge.provider.IRemoteProviderService;
import io.github.proify.lyricon.lyric.bridge.provider.ProviderInfo;

interface IRemoteProviderBinder {
    void onRegistrationCallback(IRemoteProviderService service);
    ProviderInfo getProviderInfo();
}