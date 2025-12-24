package io.github.proify.lyricon.lyric.bridge.provider;

import io.github.proify.lyricon.lyric.model.Song;

interface IRemotePlayer {
    void setSong(in Song song);
    void setPlaybackState(boolean isPlaying);
    void seekTo(int positionMs);
    void updatePosition(int positionMs);
    void sendText(String text);
}