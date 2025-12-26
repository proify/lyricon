package io.github.proify.lyricon.provider;

import android.os.SharedMemory;
import io.github.proify.lyricon.lyric.model.Song;

interface IRemotePlayer {
    void setSong(in Song song);
    void setPlaybackState(boolean isPlaying);
    void seekTo(int positionMs);
    void sendText(String text);

    void setPositionUpdateInterval(int interval);
    SharedMemory getPositionUpdateSharedMemory();
}