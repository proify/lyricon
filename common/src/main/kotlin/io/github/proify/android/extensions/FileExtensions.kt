/*
 * Lyricon – An Xposed module that extends system functionality
 * Copyright (C) 2026 Proify
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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