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