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

package io.github.proify.lyricon.app

import android.content.Context
import android.os.Handler
import com.highcapable.yukihookapi.hook.xposed.application.ModuleApplication
import io.github.proify.lyricon.app.util.AppLangUtils

class Application : ModuleApplication() {

    override fun attachBaseContext(base: Context) {
        unwrapContext = base
        super.attachBaseContext(AppLangUtils.wrap(base))
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        MAIN_HANDLER = Handler(mainLooper)
    }

    companion object {
        lateinit var MAIN_HANDLER: Handler
        lateinit var unwrapContext: Context
        lateinit var instance: Application
    }

}