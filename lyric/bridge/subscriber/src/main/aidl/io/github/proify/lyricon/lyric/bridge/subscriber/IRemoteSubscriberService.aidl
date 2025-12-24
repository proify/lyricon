package io.github.proify.lyricon.lyric.bridge.subscriber;

import io.github.proify.lyricon.lyric.bridge.subscriber.IRemoteActivePlayerListener;

interface IRemoteSubscriberService {
    void bindActivePlayerListener(in IRemoteActivePlayerListener listener);
    void disconnect();
}