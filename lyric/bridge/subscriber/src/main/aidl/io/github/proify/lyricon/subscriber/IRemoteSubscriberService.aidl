package io.github.proify.lyricon.subscriber;

import io.github.proify.lyricon.subscriber.IRemoteActivePlayerListener;

interface IRemoteSubscriberService {
    void bindActivePlayerListener(in IRemoteActivePlayerListener listener);
    void disconnect();
}