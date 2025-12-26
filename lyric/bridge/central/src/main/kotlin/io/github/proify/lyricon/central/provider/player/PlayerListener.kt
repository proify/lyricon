package io.github.proify.lyricon.central.provider.player

import io.github.proify.lyricon.lyric.model.Song

interface PlayerListener {
    fun onSongChanged(recorder: PlayerRecorder, song: Song?)
    fun onPlaybackStateChanged(recorder: PlayerRecorder, isPlaying: Boolean)
    fun onPositionChanged(recorder: PlayerRecorder, position: Int)
    fun onSeekTo(recorder: PlayerRecorder, position: Int)
    fun onPostText(recorder: PlayerRecorder, text: String?)
}