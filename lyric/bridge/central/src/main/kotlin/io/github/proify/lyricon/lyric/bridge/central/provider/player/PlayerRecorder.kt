package io.github.proify.lyricon.lyric.bridge.central.provider.player

import io.github.proify.lyricon.lyric.bridge.provider.ProviderInfo
import io.github.proify.lyricon.lyric.model.Song

class PlayerRecorder(val info: ProviderInfo) {
    var song: Song? = null
    var isPlaying: Boolean = false
    var lastPosition: Int = 0
    var text: String? = null
}