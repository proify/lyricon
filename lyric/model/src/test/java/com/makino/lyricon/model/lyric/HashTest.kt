@file:Suppress("ReplacePrintlnWithLogging")

package com.makino.lyricon.model.lyric

import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.lyric.model.lyricMetadataOf
import org.junit.Test

class HashTest {

    @Test
    fun test() {
        println(
            Song(metadata = lyricMetadataOf()) == Song(metadata = lyricMetadataOf())
        )
        println(
            lyricMetadataOf() == lyricMetadataOf()
        )
    }
}