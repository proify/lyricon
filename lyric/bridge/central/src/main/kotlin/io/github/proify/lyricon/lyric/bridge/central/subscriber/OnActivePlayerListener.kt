package io.github.proify.lyricon.lyric.bridge.central.subscriber

import io.github.proify.lyricon.lyric.bridge.provider.ProviderInfo
import io.github.proify.lyricon.lyric.model.Song

interface OnActivePlayerListener {
    fun onActiveProviderChanged(info: ProviderInfo)
    fun onSongChanged(song: Song?)
    fun onPlaybackStateChanged(isPlaying: Boolean)
    fun onPositionChanged(position: Int)
    fun onSeekTo(position: Int)
    fun onPostText(text: String?)
}