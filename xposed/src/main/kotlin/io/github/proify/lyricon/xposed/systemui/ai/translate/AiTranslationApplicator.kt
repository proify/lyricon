/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.ai.translate

import android.util.Log
import io.github.proify.lyricon.lyric.model.Song

/** 把校验后的译文（按行 index）写回 Song 歌词。 */
internal object AiTranslationApplicator {
    private const val TAG = "LyriconAiTranslator"

    fun apply(song: Song, transItems: List<TranslationItem>): Song {
        var appliedCount = 0
        val translationsByIndex = transItems.associateBy { it.index }
        val newLyrics = song.lyrics?.mapIndexed { index, line ->
            val transText = translationsByIndex[index]?.translation?.trim()

            if (!transText.isNullOrBlank()
                && line.translation.isNullOrBlank()
                && transText.lowercase() != line.text?.trim()?.lowercase()
            ) {
                appliedCount++
                line.copy(translation = transText, translationWords = null)
            } else {
                line
            }
        }
        Log.v(TAG, "Applied $appliedCount translation lines to ${song.name}")
        return song.copy(lyrics = newLyrics)
    }
}
