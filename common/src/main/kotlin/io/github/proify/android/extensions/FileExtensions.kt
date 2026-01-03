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

package io.github.proify.android.extensions

import java.io.File

/**
 * 设置权限为 644 (rw-r--r--)
 * 常用于 Android 系统配置文件、Lib 库文件
 */
fun File.setPermission644(): Boolean {
    return try {
        val r = this.setReadable(true, false)
        val w = this.setWritable(true, true) // 仅所有者可写
        val x = this.setExecutable(false, false)
        r && w && !x
    } catch (e: Exception) {
        false
    }
}

/**
 * 设置权限为 755 (rwxr-xr-x)
 * 常用于可执行文件或文件夹
 */
fun File.setPermission755(): Boolean {
    return try {
        val r = this.setReadable(true, false)
        val w = this.setWritable(true, true)
        val x = this.setExecutable(true, false)
        r && w && x
    } catch (e: Exception) {
        false
    }
}

/**
 * 设置权限为 777 (完全公开)
 *
 * 注意：在 Android 中由于 SELinux 限制，此操作可能不会如预期般生效
 */
fun File.setPermission777(): Boolean {
    return this.setReadable(true, false) &&
            this.setWritable(true, false) &&
            this.setExecutable(true, false)
}

/**
 * 针对 Android 的通用权限设置方法
 * @param ownerOnly 是否仅对所有者生效
 */
fun File.applyStandardPermissions(isDir: Boolean = this.isDirectory) {
    if (isDir) {
        this.setPermission755()
    } else {
        this.setPermission644()
    }
}