/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.util

import io.github.proify.opencc.lite.OpenCCLite

object ChineseConverter {

    fun String.toSimplified(): String {
        return OpenCCLite.T2S.convert(this)
    }

    fun String.toTraditional(): String {
        return OpenCCLite.S2T.convert(this)
    }
}
