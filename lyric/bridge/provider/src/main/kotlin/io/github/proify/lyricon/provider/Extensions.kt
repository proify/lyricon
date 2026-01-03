/*
 * Copyright 2026 Proify
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.proify.lyricon.provider

import android.os.Parcel
import android.util.ArrayMap

internal fun Parcel.writeMetadata(metadata: Map<String, String?>?) {
    if (metadata == null) {
        writeInt(-1)
        return
    }

    writeInt(metadata.size)
    metadata.forEach { (key, value) ->
        writeString(key)
        writeString(value)
    }
}

internal fun Parcel.readMetadata(): Map<String, String?>? {
    val size = readInt()
    return when {
        size < 0 -> null
        size == 0 -> emptyMap()
        else -> ArrayMap<String, String?>(size).apply {
            repeat(size) {
                val key = readString() ?: ""
                val value = readString()
                put(key, value)
            }
        }
    }
}