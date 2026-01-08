package com.makino.lyricon.model.lyric.provider

import io.github.proify.lyricon.lyric.model.interfaces.ILyricTiming

interface ILyricPositionProvider {
    fun testfind(position: Long, action: (ILyricTiming) -> Unit): Int
}