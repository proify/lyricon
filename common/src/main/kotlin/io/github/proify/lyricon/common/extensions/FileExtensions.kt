package io.github.proify.lyricon.common.extensions

import java.io.File

fun File.setPermission644() {
    this.setReadable(true, false)
    this.setWritable(true, true)
    this.setExecutable(false, false)
}