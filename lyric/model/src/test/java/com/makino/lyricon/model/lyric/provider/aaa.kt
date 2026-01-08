@file:Suppress("unused")

package com.makino.lyricon.model.lyric.provider

import io.github.proify.lyricon.lyric.model.interfaces.ILyricTiming

class aaa(
    source: List<ILyricTiming>
) : ILyricPositionProvider {
    val a: LyricPositionProvider = LyricPositionProvider(source)

    override fun testfind(
        position: Long,
        action: (ILyricTiming) -> Unit
    ): Int {
        val previousItem = a.findWithPrevious(position, action)
        return previousItem
    }
}