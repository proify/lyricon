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

package io.github.proify.lyricon.app

import android.content.Context
import android.os.Handler
import com.highcapable.yukihookapi.hook.factory.dataChannel
import com.highcapable.yukihookapi.hook.xposed.application.ModuleApplication
import io.github.proify.lyricon.app.Application.Companion.systemUIChannel
import io.github.proify.lyricon.app.bridge.AppBridgeConstants
import io.github.proify.lyricon.app.util.AppLangUtils
import io.github.proify.lyricon.common.PackageNames

class Application : ModuleApplication() {
    override fun attachBaseContext(base: Context) {
        unwrapContext = base
        super.attachBaseContext(AppLangUtils.wrap(base))
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        handler = Handler(mainLooper)
    }

    companion object {
        lateinit var handler: Handler
        lateinit var unwrapContext: Context
        lateinit var instance: Application

        val systemUIChannel by lazy {
            instance.dataChannel(packageName = PackageNames.SYSTEM_UI)
        }
    }
}

fun updateLyricStyle() {
    systemUIChannel.put(AppBridgeConstants.REQUEST_UPDATE_LYRIC_STYLE)
}