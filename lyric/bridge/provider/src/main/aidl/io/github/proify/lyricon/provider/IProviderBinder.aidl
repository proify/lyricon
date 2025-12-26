package io.github.proify.lyricon.provider;

import io.github.proify.lyricon.provider.IRemoteService;
import io.github.proify.lyricon.provider.IProviderService;
import io.github.proify.lyricon.provider.ProviderInfo;

interface IProviderBinder {
    void onRegistrationCallback(IRemoteService service);
    IProviderService getProviderService();
    ProviderInfo getProviderInfo();
}