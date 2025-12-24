package io.github.proify.lyricon.lyric.bridge.subscriber;

import io.github.proify.lyricon.lyric.bridge.subscriber.SubscriberInfo;
import io.github.proify.lyricon.lyric.bridge.subscriber.IRemoteSubscriberService;

interface IRemoteSubscriberBinder {
    void onRegistrationCallback(in IRemoteSubscriberService service);
    SubscriberInfo getSubscriberInfo();
}