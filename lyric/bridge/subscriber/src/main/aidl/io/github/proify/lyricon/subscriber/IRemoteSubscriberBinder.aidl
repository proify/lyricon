package io.github.proify.lyricon.subscriber;

import io.github.proify.lyricon.subscriber.SubscriberInfo;
import io.github.proify.lyricon.subscriber.IRemoteSubscriberService;

interface IRemoteSubscriberBinder {
    void onRegistrationCallback(in IRemoteSubscriberService service);
    SubscriberInfo getSubscriberInfo();
}