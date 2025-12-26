package io.github.proify.lyricon.central.subscriber

import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.provider.ProviderInfo

interface OnActivePlayerListener {
    fun onActiveProviderChanged(info: ProviderInfo)
    fun onSongChanged(song: Song?)
    fun onPlaybackStateChanged(isPlaying: Boolean)
    fun onPositionChanged(position: Int)
    fun onSeekTo(position: Int)
    fun onPostText(text: String?)
}