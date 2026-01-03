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

package io.github.proify.lyricon.xposed

import android.content.Context
import com.highcapable.yukihookapi.hook.log.YLog
import de.robv.android.xposed.XSharedPreferences
import io.github.proify.lyricon.common.PackageNames
import java.io.File

object Dirs {
    lateinit var moduleDataDir: File
    lateinit var tempDir: File
    lateinit var packageDir: File

    val preferenceDirectory: File? by lazy {
        XSharedPreferences(
            PackageNames.APPLICATION,
            "114514"
        ).file.parentFile
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