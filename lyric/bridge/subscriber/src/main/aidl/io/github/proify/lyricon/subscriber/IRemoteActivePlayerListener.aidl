package io.github.proify.lyricon.subscriber;

import io.github.proify.lyricon.lyric.model.Song;
import io.github.proify.lyricon.provider.ProviderInfo;

interface IRemoteActivePlayerListener {
	void onActiveProviderChanged(in ProviderInfo provider);
    void onSongChanged(in Song song);
    void onPlaybackStateChanged(boolean isPlaying);
    void onSeekTo(long position);
    void onPositionChanged(long position);
    void onPostText(String text);
}