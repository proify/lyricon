package io.github.proify.lyricon.lyric.bridge.central.provider.player

import io.github.proify.lyricon.lyric.bridge.provider.IRemotePlayer
import io.github.proify.lyricon.lyric.bridge.provider.ProviderInfo
import io.github.proify.lyricon.lyric.model.Song

class RemotePlayer(info: ProviderInfo, private val playerListener: PlayerListener) :
    IRemotePlayer.Stub() {

    companion object {
        const val TAG = "RemotePlayer"
        const val DEBUG = false
    }

    private val recorder: PlayerRecorder = PlayerRecorder(info)

    override fun setSong(song: Song?) {
        recorder.song = song
        recorder.text = null
        playerListener.onSongChanged(recorder, song)
    }

    override fun setPlaybackState(isPlaying: Boolean) {
        recorder.isPlaying = isPlaying
        playerListener.onPlaybackStateChanged(recorder, isPlaying)
    }

    override fun seekTo(position: Int) {
        recorder.lastPosition = position
        playerListener.onSeekTo(recorder, position)
    }

    override fun updatePosition(position: Int) {
        recorder.lastPosition = position
        playerListener.onPositionChanged(recorder, position)
    }

    override fun sendText(text: String?) {
        recorder.song = null
        recorder.text = text
        playerListener.onPostText(recorder, text)
    }
}