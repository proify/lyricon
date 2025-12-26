package io.github.proify.lyricon.central.provider.player

import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.provider.ProviderInfo

class PlayerRecorder(val info: ProviderInfo) {
    var song: Song? = null
    var isPlaying: Boolean = false
    var lastPosition: Int = 0
    var text: String? = null
}