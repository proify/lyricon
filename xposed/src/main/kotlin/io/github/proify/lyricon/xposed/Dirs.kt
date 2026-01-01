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

package io.github.proify.lyricon.xposed

import android.content.Context
import com.highcapable.yukihookapi.hook.log.YLog
import de.robv.android.xposed.XSharedPreferences
import io.github.proify.lyricon.app.bridge.Bridge
import io.github.proify.lyricon.common.PackageNames
import java.io.File

object Dirs {

    lateinit var moduleDataDir: File
    lateinit var tempDir: File
    lateinit var packageDir: File

    val preferenceDirectory: File by lazy {
        XSharedPreferences(
            PackageNames.APPLICATION,
            "114514"
        ).file.parentFile!!
    }

    fun getPreferenceFile(name: String): File {
        return File(preferenceDirectory, Bridge.getPreferenceFormatedFileName(name))
    }

    fun initialize(appInfo: Context) {
        val dataDir = appInfo.filesDir
        moduleDataDir = File(dataDir, "lyricon")
        tempDir = File(moduleDataDir, ".temp")
        packageDir = File(moduleDataDir, "packages")

        YLog.debug("Lyricon data directory: $moduleDataDir")
    }

    fun getDataFile(name: String): File = File(moduleDataDir, name)

    fun getTempFile(name: String): File = File(tempDir, name)

    fun getPackageDataDir(packageName: String): File = File(packageDir, packageName)

    fun getPackageDataFile(packageName: String, name: String): File =
        File(getPackageDataDir(packageName), name)
}